package net.disy.maven.enforcer.rules.concreteVersion;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import org.apache.maven.enforcer.rule.api.EnforcerRule;
import org.apache.maven.enforcer.rule.api.EnforcerRuleException;
import org.apache.maven.enforcer.rule.api.EnforcerRuleHelper;
import org.apache.maven.model.Dependency;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.component.configurator.expression.ExpressionEvaluationException;

import java.util.List;

/**
 * @author <a href="mailto:brianf@apache.org">Brian Fox</a>
 */
public class ConcreteVersionEnforcerRule
        implements EnforcerRule
{
    /**
     * Boolean, when set to true this rule fails when it finds a concrete version in the dependencies.
     */
    private boolean shouldIFail = false;

    @Override
    public void execute( EnforcerRuleHelper helper )
            throws EnforcerRuleException
    {
        Log log = helper.getLog();

        try
        {
            log.info( "Concrete Version Enforcer rule." );
            // Get the project out of the helper
            MavenProject project = (MavenProject) helper.evaluate( "${project}" );
            // Get the Original Model, not the
            List dependencies = project.getOriginalModel().getDependencies();
            for ( Object dependencyObject : dependencies )
            {
                Dependency dependency = (Dependency) dependencyObject;
                // Find concrete version, variable versions are allowed.
                if ( dependency.getVersion() != null && !dependency.getVersion().matches( "\\$\\{.*\\}" ) )
                {
                    log.warn( "Attention! Dependency " + dependency.getArtifactId()
                            + " contains concrete version: " + dependency.getVersion() );
                    if ( this.shouldIFail )
                    {
                        throw new EnforcerRuleException( "Failing because a Concrete Version was found in the " +
                                "dependencies section of the pom" );
                    }
                }
            }
        }
        catch ( ExpressionEvaluationException e )
        {
            throw new EnforcerRuleException( "Unable to lookup an expression " + e.getLocalizedMessage(), e );
        }
    }

    /**
     * If your rule is cacheable, you must return a unique id when parameters or conditions
     * change that would cause the result to be different. Multiple cached results are stored
     * based on their id.
     *
     * The easiest way to do this is to return a hash computed from the values of your parameters.
     *
     * If your rule is not cacheable, then the result here is not important, you may return anything.
     */
    public String getCacheId()
    {
        //no hash on boolean...only parameter so no hash is needed.
        return ""+this.shouldIFail;
    }

    /**
     * This tells the system if the results are cacheable at all. Keep in mind that during
     * forked builds and other things, a given rule may be executed more than once for the same
     * project. This means that even things that change from project to project may still
     * be cacheable in certain instances.
     */
    public boolean isCacheable()
    {
        return false;
    }

    /**
     * If the rule is cacheable and the same id is found in the cache, the stored results
     * are passed to this method to allow double checking of the results. Most of the time
     * this can be done by generating unique ids, but sometimes the results of objects returned
     * by the helper need to be queried. You may for example, store certain objects in your rule
     * and then query them later.
     */
    public boolean isResultValid( EnforcerRule arg0 )
    {
        return false;
    }
}

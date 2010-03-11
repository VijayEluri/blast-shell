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
package blast.shell.completer;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import blast.shell.Completer;
import org.osgi.service.command.CommandSession;

/**
 * Completer which uses the session scopes automatically appended in front of buffer.
 *
 * @version $Rev: $ $Date: $
 */
public class SessionScopeCompleter implements Completer
{

    private final CommandSession session;
    private final Completer completer;

    public SessionScopeCompleter( final CommandSession session,
                                  final Completer completer )
    {
        this.session = session;
        this.completer = completer;
    }

    public int complete( String buffer, int cursor, List<String> candidates )
    {
        // buffer could be null
        assert candidates != null;

        try
        {
            final List<Completion> completions = new ArrayList<Completion>();

            final String scope = (String) session.get( "SCOPE" );
            if( scope != null )
            {
                final String[] segments = scope.split( ":" );

                // Run completer for each segment, saving its completion results
                int max = -1;
                for( String segment : segments )
                {
                    Completion completion = new Completion( candidates );
                    completion.complete( completer, segment + ":" + buffer, ( segment + ":" ).length() + cursor );

                    // Compute the max cursor position
                    max = Math.max( max, completion.cursor );

                    completions.add( completion );
                }

                // Append candidates from completions which have the same cursor position as max
                for( Completion completion : completions )
                {
                    if( completion.cursor == max )
                    {
                        // noinspection unchecked
                        candidates.addAll( completion.candidates );
                    }
                }

                return max;
            }
        }
        catch( Exception ignore )
        {
        }
        return -1;
    }

    private class Completion
    {

        public final List<String> candidates;

        public int cursor;

        public Completion( final List candidates )
        {
            assert candidates != null;

            // noinspection unchecked
            this.candidates = new LinkedList<String>( candidates );
        }

        public void complete( final Completer completer, final String buffer, final int cursor )
        {
            assert completer != null;

            this.cursor = completer.complete( buffer, cursor, candidates );
        }
    }

}

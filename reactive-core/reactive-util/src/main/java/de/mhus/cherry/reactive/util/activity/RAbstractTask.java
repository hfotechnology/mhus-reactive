/**
 * Copyright (C) 2020 Mike Hummel (mh@mhus.de)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.mhus.cherry.reactive.util.activity;

import de.mhus.cherry.reactive.model.activity.AActivity;
import de.mhus.cherry.reactive.model.activity.ATask;
import de.mhus.cherry.reactive.model.engine.PNode.STATE_NODE;
import de.mhus.cherry.reactive.model.errors.EngineException;
import de.mhus.cherry.reactive.model.util.ActivityUtil;
import de.mhus.cherry.reactive.util.bpmn2.RPool;

public abstract class RAbstractTask<P extends RPool<?>> extends RActivity<P> implements ATask<P> {

    @Override
    public void doExecuteActivity() throws Exception {
        String nextName = doExecute();
        if (nextName == null) nextName = DEFAULT_OUTPUT;
        if (!nextName.equals(RETRY)) {
            Class<? extends AActivity<?>> next = ActivityUtil.getOutputByName(this, nextName);
            if (next == null)
                throw new EngineException(
                        "Output Activity not found: "
                                + nextName
                                + " in "
                                + getClass().getCanonicalName());
            getContext().createActivity(next);
            getContext().getPNode().setState(STATE_NODE.CLOSED);
        }
    }

    public abstract String doExecute() throws Exception;
}

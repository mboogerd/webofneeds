/*
 * Copyright 2012  Research Studios Austria Forschungsges.m.b.H.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package won.bot.framework.events.event.impl;

import won.bot.framework.events.event.BaseEvent;
import won.bot.framework.events.listener.EventListener;

/**
 * Event to be published when an error occurs.
 */
public class ErrorEvent extends BaseEvent
{
  private EventListener listenerInError;
  private Throwable throwable;

  public ErrorEvent(final EventListener listenerInError, final Throwable throwable) {
    this.listenerInError = listenerInError;
    this.throwable = throwable;
  }

  public EventListener getListenerInError() {
    return listenerInError;
  }

  public void setListenerInError(final EventListener listenerInError) {
    this.listenerInError = listenerInError;
  }

  public Throwable getThrowable() {
    return throwable;
  }

  public void setThrowable(final Throwable throwable) {
    this.throwable = throwable;
  }
}
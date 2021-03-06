package kz.greetgo.scheduling.trigger.inner_logic;

import kz.greetgo.scheduling.trigger.TriggerParseError;

import java.util.List;

public interface TriggerParseResult {

  Trigger trigger();

  List<TriggerParseError> errors();

}

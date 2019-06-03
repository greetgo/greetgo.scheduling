package kz.greetgo.scheduling.collector;

import kz.greetgo.scheduling.trigger.inner_logic.Trigger;
import kz.greetgo.util.RND;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ControllerContextTest {

  @Test
  public void trigger_creationFirstTime() {

    long[] currentTime = new long[]{100};
    MemoryFileContent configFile = new MemoryFileContent(() -> currentTime[0]);
    MemoryFileContent errorFile = new MemoryFileContent(() -> currentTime[0]);

    String helpLine1 = RND.str(10);
    String helpLine2 = RND.str(10);
    String helpLine3 = RND.str(10);

    String headerHelp = helpLine1 + "\n" + helpLine2 + "\n" + helpLine3;

    long checkFileDelayMillis = 500;

    ControllerContext context = new ControllerContext(
      configFile, errorFile, headerHelp, checkFileDelayMillis, () -> currentTime[0]
    );

    assertThat(configFile.exists()).isFalse();

    String confLine1 = RND.str(10);
    String confLine2 = RND.str(10);

    ScheduledDefinition definition = new ScheduledDefinition("name1", "11:21", true, confLine1 + "\n" + confLine2);

    currentTime[0] += 100;

    Trigger trigger1 = context.trigger(definition);
    System.out.println("43h26v :: trigger1 = " + trigger1);

    assertThat("" + trigger1).contains("11:21:00");

    assertThat(configFile.lastModifiedAt).isEqualTo(100);
    String[] configLines = configFile.content.split("\n");
    assertThat(configLines).contains("# " + helpLine1);
    assertThat(configLines).contains("# " + helpLine2);
    assertThat(configLines).contains("# " + helpLine3);
    assertThat(configLines).contains("# " + confLine1);
    assertThat(configLines).contains("# " + confLine2);
    assertThat(configLines).contains("name1 = 11:21");
  }

  @Test
  public void trigger_creationFirstTime__headerHelpIsNull() {

    long[] currentTime = new long[]{100};
    MemoryFileContent configFile = new MemoryFileContent(() -> currentTime[0]);
    MemoryFileContent errorFile = new MemoryFileContent(() -> currentTime[0]);

    long checkFileDelayMillis = 500;

    ControllerContext context = new ControllerContext(
      configFile, errorFile, null, checkFileDelayMillis, () -> currentTime[0]
    );

    assertThat(configFile.exists()).isFalse();

    String confLine1 = RND.str(10);
    String confLine2 = RND.str(10);

    ScheduledDefinition definition = new ScheduledDefinition("name1", "11:21", true, confLine1 + "\n" + confLine2);

    currentTime[0] += 100;

    Trigger trigger = context.trigger(definition);
    System.out.println("43h26v :: trigger = " + trigger);

    assertThat("" + trigger).contains("11:21:00");

    assertThat(configFile.lastModifiedAt).isEqualTo(100);
    String[] configLines = configFile.content.split("\n");
    assertThat(configLines).contains("# " + confLine1);
    assertThat(configLines).contains("# " + confLine2);
    assertThat(configLines).contains("name1 = 11:21");
  }

  @Test
  public void trigger__afterFirstCreation_secondFastCallDoesNotCheckFileModification() {
    long[] currentTime = new long[]{100};
    MemoryFileContent configFile = new MemoryFileContent(() -> currentTime[0]);
    MemoryFileContent errorFile = new MemoryFileContent(() -> currentTime[0]);

    long checkFileDelayMillis = 500;

    ControllerContext context = new ControllerContext(
      configFile, errorFile, null, checkFileDelayMillis, () -> currentTime[0]
    );

    assertThat(configFile.exists()).isFalse();

    String confLine1 = RND.str(10);
    String confLine2 = RND.str(10);

    ScheduledDefinition definition = new ScheduledDefinition("name1", "11:21", true, confLine1 + "\n" + confLine2);

    currentTime[0] += 100;

    //
    //
    context.trigger(definition);
    //
    //

    currentTime[0] = 300;
    configFile.lastModifiedAtCalled = false;

    //
    //
    context.trigger(definition);
    //
    //

    assertThat(configFile.lastModifiedAtCalled).isFalse();

  }


  @Test
  public void trigger__afterFirstCreation_secondSlowCallDoCheckFileModification() {
    long[] currentTime = new long[]{100};
    MemoryFileContent configFile = new MemoryFileContent(() -> currentTime[0]);
    MemoryFileContent errorFile = new MemoryFileContent(() -> currentTime[0]);

    long checkFileDelayMillis = 500;

    ControllerContext context = new ControllerContext(
      configFile, errorFile, null, checkFileDelayMillis, () -> currentTime[0]
    );

    assertThat(configFile.exists()).isFalse();

    String confLine1 = RND.str(10);
    String confLine2 = RND.str(10);

    ScheduledDefinition definition = new ScheduledDefinition("name1", "11:21", true, confLine1 + "\n" + confLine2);

    currentTime[0] += 100;

    //
    //
    context.trigger(definition);
    //
    //

    currentTime[0] = 200 + 500 + 1;
    configFile.lastModifiedAtCalled = false;

    //
    //
    context.trigger(definition);
    //
    //

    assertThat(configFile.lastModifiedAtCalled).isTrue();

  }

  @Test
  public void trigger__readFromFileAtFirstCall_foundMyLine() {
    long[] currentTime = new long[]{100};
    MemoryFileContent configFile = new MemoryFileContent(() -> currentTime[0]);
    MemoryFileContent errorFile = new MemoryFileContent(() -> currentTime[0]);

    configFile.content = "" +
      "name1 = 23:17\n" +
      "name2 = 14:45\n";
    configFile.lastModifiedAt = currentTime[0];

    long checkFileDelayMillis = 500;

    ControllerContext context = new ControllerContext(
      configFile, errorFile, null, checkFileDelayMillis, () -> currentTime[0]
    );

    assertThat(configFile.exists()).isFalse();

    String confLine1 = RND.str(10);
    String confLine2 = RND.str(10);

    ScheduledDefinition definition = new ScheduledDefinition("name1", "11:21", true, confLine1 + "\n" + confLine2);

    currentTime[0] += 100;

    //
    //
    Trigger trigger = context.trigger(definition);
    //
    //

    assertThat("" + trigger).doesNotContain("11:21");
    assertThat("" + trigger).contains("23:17:00");
  }

  @Test
  public void trigger__readFromFileAtFirstCall_notFoundMyLine() {
    long[] currentTime = new long[]{100};
    MemoryFileContent configFile = new MemoryFileContent(() -> currentTime[0]);
    MemoryFileContent errorFile = new MemoryFileContent(() -> currentTime[0]);

    configFile.content = "" +
      "name10 = 23:17\n" +
      "name20 = 14:45\n";
    configFile.lastModifiedAt = currentTime[0];

    long checkFileDelayMillis = 500;

    ControllerContext context = new ControllerContext(
      configFile, errorFile, null, checkFileDelayMillis, () -> currentTime[0]
    );

    assertThat(configFile.exists()).isFalse();

    String confLine1 = RND.str(10);
    String confLine2 = RND.str(10);

    ScheduledDefinition definition = new ScheduledDefinition("name1", "11:21", true, confLine1 + "\n" + confLine2);

    currentTime[0] += 100;

    assertThat(configFile.content.split("\n")).doesNotContain("name1 = 11:21");

    //
    //
    Trigger trigger = context.trigger(definition);
    //
    //

    assertThat(configFile.content.split("\n")).contains("# " + confLine1);
    assertThat(configFile.content.split("\n")).contains("# " + confLine2);
    assertThat(configFile.content.split("\n")).contains("name1 = 11:21");

    assertThat("" + trigger).contains("11:21:00");

  }

  @Test
  public void trigger__readFile__nextFastCallDoNotCheckFileModificationTime() {
    long[] currentTime = new long[]{100};
    MemoryFileContent configFile = new MemoryFileContent(() -> currentTime[0]);
    MemoryFileContent errorFile = new MemoryFileContent(() -> currentTime[0]);

    configFile.content = "" +
      "name1 = 23:17\n" +
      "name2 = 14:45\n";
    configFile.lastModifiedAt = currentTime[0];

    long checkFileDelayMillis = 500;

    ControllerContext context = new ControllerContext(
      configFile, errorFile, null, checkFileDelayMillis, () -> currentTime[0]
    );

    assertThat(configFile.exists()).isFalse();

    String confLine1 = RND.str(10);
    String confLine2 = RND.str(10);

    ScheduledDefinition definition = new ScheduledDefinition("name1", "11:21", true, confLine1 + "\n" + confLine2);

    currentTime[0] += 100;

    //
    //
    context.trigger(definition);
    //
    //

    currentTime[0] += checkFileDelayMillis - 3;
    configFile.lastModifiedAtCalled = false;

    //
    //
    context.trigger(definition);
    //
    //

    assertThat(configFile.lastModifiedAtCalled).isFalse();

  }

  @Test
  public void trigger__readFile__nextSlowCallChecksFileModificationTime_doNotLoadFileBecauseTimeSame() {
    long[] currentTime = new long[]{100};
    MemoryFileContent configFile = new MemoryFileContent(() -> currentTime[0]);
    MemoryFileContent errorFile = new MemoryFileContent(() -> currentTime[0]);

    configFile.content = "" +
      "name1 = 23:17\n" +
      "name2 = 14:45\n";
    configFile.lastModifiedAt = currentTime[0];

    long checkFileDelayMillis = 500;

    ControllerContext context = new ControllerContext(
      configFile, errorFile, null, checkFileDelayMillis, () -> currentTime[0]
    );

    assertThat(configFile.exists()).isFalse();

    String confLine1 = RND.str(10);
    String confLine2 = RND.str(10);

    ScheduledDefinition definition = new ScheduledDefinition("name1", "11:21", true, confLine1 + "\n" + confLine2);

    currentTime[0] += 100;

    //
    //
    context.trigger(definition);
    //
    //

    currentTime[0] += checkFileDelayMillis + 3;
    configFile.lastModifiedAtCalled = false;
    configFile.getCalled = false;

    //
    //
    context.trigger(definition);
    //
    //

    assertThat(configFile.lastModifiedAtCalled).isTrue();
    assertThat(configFile.getCalled).isFalse();

  }

  @Test
  public void trigger__readFile__nextSlowCallDoCheckFileModificationTime_loadFileBecauseModTimeChanged() {
    long[] currentTime = new long[]{100};
    MemoryFileContent configFile = new MemoryFileContent(() -> currentTime[0]);
    MemoryFileContent errorFile = new MemoryFileContent(() -> currentTime[0]);

    configFile.content = "name1 = 23:17";
    configFile.lastModifiedAt = currentTime[0];

    long checkFileDelayMillis = 500;

    ControllerContext context = new ControllerContext(
      configFile, errorFile, null, checkFileDelayMillis, () -> currentTime[0]
    );

    assertThat(configFile.exists()).isFalse();

    String confLine1 = RND.str(10);
    String confLine2 = RND.str(10);

    ScheduledDefinition definition = new ScheduledDefinition("name1", "11:21", true, confLine1 + "\n" + confLine2);

    currentTime[0] += 100;

    //
    //
    Trigger trigger1 = context.trigger(definition);
    //
    //

    assertThat("" + trigger1).contains("23:17:45");

    currentTime[0] += 100;
    configFile.content = "name1 = 17:17:17";
    configFile.lastModifiedAt = currentTime[0];


    currentTime[0] += checkFileDelayMillis + 3;
    configFile.lastModifiedAtCalled = false;
    configFile.getCalled = false;

    //
    //
    Trigger trigger2 = context.trigger(definition);
    //
    //

    assertThat(configFile.lastModifiedAtCalled).isTrue();
    assertThat(configFile.getCalled).isTrue();

    assertThat("" + trigger2).contains("17:17:17");
  }

  @Test
  public void trigger__readFile_writeErrorFile__myTriggerIsOk() {
    long[] currentTime = new long[]{100};
    MemoryFileContent configFile = new MemoryFileContent(() -> currentTime[0]);
    MemoryFileContent errorFile = new MemoryFileContent(() -> currentTime[0]);

    configFile.content = "" +
      "name1 = 23:17\n" +
      "name2 = left trigger\n";
    configFile.lastModifiedAt = currentTime[0];

    long checkFileDelayMillis = 500;

    ControllerContext context = new ControllerContext(
      configFile, errorFile, null, checkFileDelayMillis, () -> currentTime[0]
    );

    assertThat(configFile.exists()).isFalse();

    String confLine1 = RND.str(10);
    String confLine2 = RND.str(10);

    ScheduledDefinition definition = new ScheduledDefinition("name1", "11:21", true, confLine1 + "\n" + confLine2);

    currentTime[0] += 100;

    //
    //
    Trigger trigger = context.trigger(definition);
    //
    //

    System.out.println("h5b426v54 :: errorFile :\n" + errorFile.content);

    assertThat("" + trigger).contains("23:17:00");

    assertThat(errorFile.lastModifiedAt).isEqualTo(currentTime[0]);

    String[] errorLines = errorFile.content.split("\n");

    assertThat(errorLines).contains("Ошибка");

  }

  @Test
  public void trigger__readFile_writeErrorFile__myTriggerIsBroken() {
    long[] currentTime = new long[]{100};
    MemoryFileContent configFile = new MemoryFileContent(() -> currentTime[0]);
    MemoryFileContent errorFile = new MemoryFileContent(() -> currentTime[0]);

    configFile.content = "" +
      "name1 = left\n" +
      "name2 = left trigger\n";
    configFile.lastModifiedAt = currentTime[0];

    long checkFileDelayMillis = 500;

    ControllerContext context = new ControllerContext(
      configFile, errorFile, null, checkFileDelayMillis, () -> currentTime[0]
    );

    assertThat(configFile.exists()).isFalse();

    String confLine1 = RND.str(10);
    String confLine2 = RND.str(10);

    ScheduledDefinition definition = new ScheduledDefinition("name1", "11:21", true, confLine1 + "\n" + confLine2);

    currentTime[0] += 100;

    //
    //
    Trigger trigger = context.trigger(definition);
    //
    //

    System.out.println("g6v543c7 :: errorFile :\n" + errorFile.content);

    assertThat("" + trigger).contains("SILENT");

    assertThat(errorFile.lastModifiedAt).isEqualTo(currentTime[0]);

    String[] errorLines = errorFile.content.split("\n");

    assertThat(errorLines).contains("Ошибка");
  }

  @Test
  public void trigger__readFile_removeErrorFileBecauseOfNoErrors() {
    long[] currentTime = new long[]{100};
    MemoryFileContent configFile = new MemoryFileContent(() -> currentTime[0]);
    MemoryFileContent errorFile = new MemoryFileContent(() -> currentTime[0]);

    configFile.content = "" +
      "name1 = 16:11\n" +
      "name2 = 21:00\n";
    configFile.lastModifiedAt = currentTime[0];

    errorFile.content = RND.str(10);

    long checkFileDelayMillis = 500;

    ControllerContext context = new ControllerContext(
      configFile, errorFile, null, checkFileDelayMillis, () -> currentTime[0]
    );

    assertThat(configFile.exists()).isFalse();

    String confLine1 = RND.str(10);
    String confLine2 = RND.str(10);

    ScheduledDefinition definition = new ScheduledDefinition("name1", "11:21", true, confLine1 + "\n" + confLine2);

    currentTime[0] += 100;

    //
    //
    Trigger trigger = context.trigger(definition);
    //
    //

    System.out.println("g6v543c7 :: errorFile :\n" + errorFile.content);

    assertThat("" + trigger).contains("16:11:00");

    assertThat(errorFile.lastModifiedAt).isEqualTo(currentTime[0]);

    String[] errorLines = errorFile.content.split("\n");

    assertThat(errorLines).contains("Ошибка");
  }

}

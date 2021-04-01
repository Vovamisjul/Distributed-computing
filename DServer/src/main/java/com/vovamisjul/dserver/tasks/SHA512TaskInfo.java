package com.vovamisjul.dserver.tasks;

public class SHA512TaskInfo extends TaskInfo {

    @Override
    public String getId() {
        return "123";
    }

    @Override
    public String getDescription() {
        return "Simple task for brooting many strings to find, " +
                "which ones sha512 hash matches required one.";
    }

    @Override
    public AbstractTaskController createTaskController() {
        return new SHA512FinderController(getId());
    }
}

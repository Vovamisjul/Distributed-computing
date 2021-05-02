package com.vovamisjul.dserver.tasks;

public class SHA512TaskInfo extends TaskInfo {

    @Override
    public String getId() {
        return "4b5678b1-1b68-4462-8077-443d23a62464";
    }

    @Override
    public String getName() {
        return "SHA512 brooter";
    }

    @Override
    public String getDescription() {
        return "Simple task for brooting many strings to find, " +
                "which ones SHA512 hash matches required one. (UTF-8 encoding). " +
                "More about SHA and hashes - https://en.wikipedia.org/wiki/SHA-2";
    }

    @Override
    public String[] getParamsDescription() {
        return new String[] {"Required hash", "Max hashed string length"};
    }

    @Override
    public AbstractTaskController createTaskController() {
        return new SHA512FinderController(getId());
    }

    @Override
    public AbstractTaskController createTaskController(String copyId) {
        return new SHA512FinderController(getId(), copyId);
    }
}

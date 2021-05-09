package com.vovamisjul.dserver.tasks;

import tasks.AbstractTaskController;
import tasks.TaskInfo;

public class FactorialPrimesTaskInfo extends TaskInfo {
    @Override
    public String getId() {
        return "11500796-d36e-4e69-a320-ac78e8f07e69";
    }

    @Override
    public String getName() {
        return "Factorial prime numbers finder";
    }

    @Override
    public String getDescription() {
        return "Finds factorial prime numbers. " +
                "A factorial prime is a prime number that is one less or one more than a factorial (such prime p, that p = x!±1, x ∈ N) " +
                "(all factorials greater than 1 are even). " +
                "More - https://en.wikipedia.org/wiki/Factorial_prime";
    }

    @Override
    public String[] getParamsDescription() {
        return new String[]{"Maximum factorial base"};
    }

    @Override
    public AbstractTaskController createTaskController() {
        return null;
    }

    @Override
    public AbstractTaskController createTaskController(String copyId) {
        return null;
    }
}

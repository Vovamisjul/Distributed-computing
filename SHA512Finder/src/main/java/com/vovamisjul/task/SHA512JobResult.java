package com.vovamisjul.task;

import tasks.JobResult;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

public class SHA512JobResult extends JobResult implements Iterable<String> {
    private final boolean found;
    private final List<String> answers;

    public SHA512JobResult(boolean found, List<String> answers) {
        this.found = found;
        this.answers = answers;
    }

    public SHA512JobResult(boolean found) {
        this.found = found;
        this.answers = new ArrayList<>();
    }

    public void addAnswer(String answer) {
        answers.add(answer);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SHA512JobResult jobResult = (SHA512JobResult) o;
        return found == jobResult.found && answers.equals(jobResult.answers);
    }

    @Override
    public int hashCode() {
        return Objects.hash(found, answers);
    }

    @Nonnull
    @Override
    public Iterator<String> iterator() {
        return answers.iterator();
    }
}

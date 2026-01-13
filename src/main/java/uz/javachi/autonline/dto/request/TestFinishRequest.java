package uz.javachi.autonline.dto.request;

public record TestFinishRequest(
        Long testResultId,
        Integer score,
        Integer correctCount,
        Integer wrongCount
) {}
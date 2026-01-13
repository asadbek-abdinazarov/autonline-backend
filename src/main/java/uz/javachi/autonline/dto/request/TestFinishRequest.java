package uz.javachi.autonline.dto.request;

public record TestFinishRequest(
        Long testResultId,
        Integer score,
        Integer percentage,
        Integer correctCount,
        Integer wrongCount
) {}
package com.example.sca_be.domain.ai.config;

/**
 * AI 관련 상수 클래스
 * 파이썬 config.py를 Java로 이식
 */
public class AIConstants {

    // ========== 점수 범위 ==========
    public static final int COGNITIVE_SCORE_MIN = 1;
    public static final int COGNITIVE_SCORE_MAX = 6;
    public static final int EFFORT_SCORE_MIN = 1;
    public static final int EFFORT_SCORE_MAX = 10;

    // ========== 보상 계산 가중치 ==========
    // exploration_data = (cognitive² × EXPLORATION_COGNITIVE_WEIGHT) + (effort × EXPLORATION_EFFORT_WEIGHT)
    public static final int EXPLORATION_COGNITIVE_WEIGHT = 5;
    public static final int EXPLORATION_EFFORT_WEIGHT = 2;

    // coral = (effort × CORAL_EFFORT_WEIGHT) + (cognitive × CORAL_COGNITIVE_WEIGHT)
    public static final int CORAL_EFFORT_WEIGHT = 5;
    public static final int CORAL_COGNITIVE_WEIGHT = 2;

    // ========== 학습 파라미터 ==========
    // 학습률 (Learning Rate)
    public static final double LEARNING_RATE_OVERRIDE = 0.3;    // 20% 이상 수정
    public static final double LEARNING_RATE_FINE_TUNE = 0.1;   // 5-20% 수정
    public static final double LEARNING_RATE_MINOR = 0.05;      // 5% 이하 수정

    // ========== 보정계수 범위 ==========
    public static final double FACTOR_MIN = 0.8;
    public static final double FACTOR_MAX = 1.2;
    public static final double INITIAL_FACTOR_MIN = 0.9;
    public static final double INITIAL_FACTOR_MAX = 1.1;

    // ========== 수정 유형 분류 임계값 ==========
    public static final double OVERRIDE_THRESHOLD = 0.2;      // 20%
    public static final double FINE_TUNE_THRESHOLD = 0.05;    // 5%

    // ========== 초기화 파라미터 ==========
    public static final int BASELINE_SCORE = 75;     // 기준 성적
    public static final int COLD_START_WEEKS = 3;    // 학기 초 보수적 학습 기간 (주)

    // ========== 계수 가중치 ==========
    // 최종 계수 = (DIFFICULTY_WEIGHT × questFactor) + (GLOBAL_WEIGHT × globalFactor)
    public static final double DIFFICULTY_WEIGHT = 0.6;
    public static final double GLOBAL_WEIGHT = 0.4;

    // ========== 학습 시 보상 가중평균 비율 ==========
    // actualRatio = (explorationRatio × EXPLORATION_REWARD_WEIGHT) + (coralRatio × CORAL_REWARD_WEIGHT)
    public static final double EXPLORATION_REWARD_WEIGHT = 0.5;  // 탐사 데이터 가중치
    public static final double CORAL_REWARD_WEIGHT = 0.5;        // 코랄 가중치

    // ========== 난이도 매핑 (Enum ordinal과 DB 값) ==========
    // EASY(1), BASIC(2), MEDIUM(3), HARD(4), VERY_HARD(5)
    public static final int DIFFICULTY_EASY = 1;
    public static final int DIFFICULTY_BASIC = 2;
    public static final int DIFFICULTY_MEDIUM = 3;
    public static final int DIFFICULTY_HARD = 4;
    public static final int DIFFICULTY_VERY_HARD = 5;

    private AIConstants() {
        // 인스턴스 생성 방지
        throw new AssertionError("Cannot instantiate constants class");
    }
}

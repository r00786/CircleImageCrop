package circlecrop.rohit.image.gpuimage;

public enum Rotation {
    NORMAL,
    ROTATION_90,
    ROTATION_180,
    ROTATION_270;

    public int asInt() {
        switch (this) {
            case NORMAL:
                return 0;
            case ROTATION_90:
                return 90;
            case ROTATION_180:
                return 180;
            case ROTATION_270:
                return 270;
            default:
                throw new IllegalStateException("Unknown Rotation!");
        }
    }

    public Rotation clockwiseNext() {
        switch (this) {
            case NORMAL:
                return ROTATION_90;
            case ROTATION_90:
                return ROTATION_180;
            case ROTATION_180:
                return ROTATION_270;
            case ROTATION_270:
                return NORMAL;
            default:
                throw new IllegalStateException("Unknown Rotation!");
        }
    }

    public Rotation counterClockwiseNext() {
        switch (this) {
            case NORMAL:
                return ROTATION_270;
            case ROTATION_90:
                return NORMAL;
            case ROTATION_180:
                return ROTATION_90;
            case ROTATION_270:
                return ROTATION_180;
            default:
                throw new IllegalStateException("Unknown Rotation!");
        }
    }
}

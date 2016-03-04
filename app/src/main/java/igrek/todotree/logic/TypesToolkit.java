package igrek.todotree.logic;

public class TypesToolkit {
    public static boolean isFlagSet(int tested, int flag) {
        return (tested & flag) == flag;
    }

    public class Align {
        public static final int DEFAULT = 0x000;
        //  Pozycja
        public static final int LEFT = 0x001;
        public static final int RIGHT = 0x002;
        public static final int HCENTER = 0x004;
        public static final int TOP = 0x010;
        public static final int BOTTOM = 0x020;
        public static final int VCENTER = 0x040;
        // mieszane
        public static final int CENTER = HCENTER | VCENTER;
        public static final int BOTTOM_LEFT = BOTTOM | LEFT;
        public static final int BOTTOM_RIGHT = BOTTOM | RIGHT;
        public static final int BOTTOM_HCENTER = BOTTOM | HCENTER;
        //  Rozmiar
        public static final int HADJUST = 0x100;
        public static final int VADJUST = 0x200;
        public static final int ADJUST = HADJUST | VADJUST;
    }

    public class Font {
        //family
        public static final int FONT_DEFAULT = 0x01;
        public static final int FONT_MONOSPACE = 0x02;
        //style
        public static final int FONT_NORMAL = 0x10;
        public static final int FONT_BOLD = 0x20;
    }
}

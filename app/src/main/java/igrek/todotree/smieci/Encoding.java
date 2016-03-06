package igrek.todotree.smieci;

class Encoding{
    static byte polish_to_ansi(char p){
        if(p=='ą') return (byte)185;
        if(p=='ż') return (byte)191;
        if(p=='ś') return (byte)156;
        if(p=='ź') return (byte)159;
        if(p=='ę') return (byte)234;
        if(p=='ć') return (byte)230;
        if(p=='ń') return (byte)241;
        if(p=='ó') return (byte)243;
        if(p=='ł') return (byte)179;
        if(p=='Ą') return (byte)165;
        if(p=='Ż') return (byte)175;
        if(p=='Ś') return (byte)140;
        if(p=='Ź') return (byte)143;
        if(p=='Ę') return (byte)202;
        if(p=='Ć') return (byte)198;
        if(p=='Ń') return (byte)209;
        if(p=='Ó') return (byte)211;
        if(p=='Ł') return (byte)163;
        return (byte)p;
    }
    static char ansi_to_polish(char a){
        if(a==185) return 'ą';
        if(a==191) return 'ż';
        if(a==156) return 'ś';
        if(a==159) return 'ź';
        if(a==234) return 'ę';
        if(a==230) return 'ć';
        if(a==241) return 'ń';
        if(a==243) return 'ó';
        if(a==179) return 'ł';
        if(a==165) return 'Ą';
        if(a==175) return 'Ż';
        if(a==140) return 'Ś';
        if(a==143) return 'Ź';
        if(a==202) return 'Ę';
        if(a==198) return 'Ć';
        if(a==209) return 'Ń';
        if(a==211) return 'Ó';
        if(a==163) return 'Ł';
        return a;
    }
    static char to_uppercase(char c){
        if(c>='a'&&c<='z') c-='a'-'A';
        if(c=='ą') return 'Ą';
        if(c=='ż') return 'Ż';
        if(c=='ś') return 'Ś';
        if(c=='ź') return 'Ź';
        if(c=='ę') return 'Ę';
        if(c=='ć') return 'Ć';
        if(c=='ń') return 'Ń';
        if(c=='ó') return 'Ó';
        if(c=='ł') return 'Ł';
        return c;
    }
}
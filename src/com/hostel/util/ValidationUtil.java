package com.hostel.util;

import com.hostel.exception.HostelException;

public final class ValidationUtil {
    private ValidationUtil(){}
    public static void require(boolean c,String m)throws HostelException{if(!c)throw new HostelException(m);}
    public static void text(String v,String f)throws HostelException{require(v!=null&&!v.trim().isEmpty(),f+" cannot be empty.");}
    public static void positive(int v,String f)throws HostelException{require(v>0,f+" must be greater than 0.");}
    public static void email(String v)throws HostelException{
        text(v,"Email"); require(v.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$"),"Invalid email address.");
    }
}

package org.csapi.pam;

/**
 *	Generated from IDL definition of exception "P_PAM_UNKNOWN_IDENTITY"
 *	@author JacORB IDL compiler 
 */

public final class P_PAM_UNKNOWN_IDENTITY
	extends org.omg.CORBA.UserException
{
	public P_PAM_UNKNOWN_IDENTITY()
	{
		super(org.csapi.pam.P_PAM_UNKNOWN_IDENTITYHelper.id());
	}

	public java.lang.String ExtraInformation;
	public P_PAM_UNKNOWN_IDENTITY(java.lang.String _reason,java.lang.String ExtraInformation)
	{
		super(org.csapi.pam.P_PAM_UNKNOWN_IDENTITYHelper.id()+""+_reason);
		this.ExtraInformation = ExtraInformation;
	}
	public P_PAM_UNKNOWN_IDENTITY(java.lang.String ExtraInformation)
	{
		this.ExtraInformation = ExtraInformation;
	}
}

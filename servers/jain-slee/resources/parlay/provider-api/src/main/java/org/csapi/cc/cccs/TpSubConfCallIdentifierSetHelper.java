package org.csapi.cc.cccs;

/**
 *	Generated from IDL definition of alias "TpSubConfCallIdentifierSet"
 *	@author JacORB IDL compiler 
 */

public final class TpSubConfCallIdentifierSetHelper
{
	private static org.omg.CORBA.TypeCode _type = null;

	public static void insert (org.omg.CORBA.Any any, org.csapi.cc.cccs.TpSubConfCallIdentifier[] s)
	{
		any.type (type ());
		write (any.create_output_stream (), s);
	}

	public static org.csapi.cc.cccs.TpSubConfCallIdentifier[] extract (final org.omg.CORBA.Any any)
	{
		return read (any.create_input_stream ());
	}

	public static org.omg.CORBA.TypeCode type ()
	{
		if (_type == null)
		{
			_type = org.omg.CORBA.ORB.init().create_alias_tc(org.csapi.cc.cccs.TpSubConfCallIdentifierSetHelper.id(), "TpSubConfCallIdentifierSet",org.omg.CORBA.ORB.init().create_sequence_tc(0, org.csapi.cc.cccs.TpSubConfCallIdentifierHelper.type()));
		}
		return _type;
	}

	public static String id()
	{
		return "IDL:org/csapi/cc/cccs/TpSubConfCallIdentifierSet:1.0";
	}
	public static org.csapi.cc.cccs.TpSubConfCallIdentifier[] read (final org.omg.CORBA.portable.InputStream _in)
	{
		org.csapi.cc.cccs.TpSubConfCallIdentifier[] _result;
		int _l_result4 = _in.read_long();
		_result = new org.csapi.cc.cccs.TpSubConfCallIdentifier[_l_result4];
		for (int i=0;i<_result.length;i++)
		{
			_result[i]=org.csapi.cc.cccs.TpSubConfCallIdentifierHelper.read(_in);
		}

		return _result;
	}

	public static void write (final org.omg.CORBA.portable.OutputStream _out, org.csapi.cc.cccs.TpSubConfCallIdentifier[] _s)
	{
		
		_out.write_long(_s.length);
		for (int i=0; i<_s.length;i++)
		{
			org.csapi.cc.cccs.TpSubConfCallIdentifierHelper.write(_out,_s[i]);
		}

	}
}

package org.omg.PortableServer.POAPackage;


/**
* org/omg/PortableServer/POAPackage/ServantNotActive.java .
* Generated by the IDL-to-Java compiler (portable), version "3.2"
* from d:/re/workspace/8-2-build-windows-amd64-cygwin/jdk8u25/1677/corba/src/share/classes/org/omg/PortableServer/poa.idl
* Tuesday, October 7, 2014 2:27:05 PM PDT
*/

public final class ServantNotActive extends org.omg.CORBA.UserException
{

  public ServantNotActive ()
  {
    super(ServantNotActiveHelper.id());
  } // ctor


  public ServantNotActive (String $reason)
  {
    super(ServantNotActiveHelper.id() + "  " + $reason);
  } // ctor

} // class ServantNotActive

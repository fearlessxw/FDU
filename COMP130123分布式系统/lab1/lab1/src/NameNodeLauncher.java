import java.util.ArrayList;
import java.util.List;
import java.util.*;

import org.omg.CORBA.ORB;
import org.omg.PortableServer.*;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;
import org.omg.CosNaming.NameComponent;

import impl.NameNodeImpl;
import api.*;

public class NameNodeLauncher {
    public static void main(String[] args) {
        try {
            Properties properties = new Properties();
            properties.put("org.omg.COBRA.ORBInitialHost", "127.0.0.1");
            properties.put("org.omg.CORBA.ORBInitialPort", "1050");

            ORB orb = ORB.init(args, properties);

            // get RootPOA activate POAManager
            POA rootpoa = POAHelper.narrow(orb.resolve_initial_references ("RootPOA"));
            rootpoa.the_POAManager().activate();

            // new an object
            NameNodeImpl nameNodeServant = new NameNodeImpl();


            // export
            org.omg.CORBA.Object ref = rootpoa.servant_to_reference(nameNodeServant);
            NameNode href = NameNodeHelper.narrow(ref);

            // Naming context
            org.omg.CORBA.Object objRef = orb.resolve_initial_references("NameService");
            NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef) ;

            // bind to Naming
            NameComponent[] path = ncRef.to_name("NameNode");
            ncRef.rebind(path, href);
            System.out.println("NameNode is ready and waiting...");

            // waiting
            orb.run();


        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

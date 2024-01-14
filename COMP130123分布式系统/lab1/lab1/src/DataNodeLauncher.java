import java.util.ArrayList;
import java.util.List;
import java.util.*;

import org.omg.CORBA.ORB;
import org.omg.PortableServer.*;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;
import org.omg.CosNaming.NameComponent;

import impl.DataNodeImpl;
import api.*;

public class DataNodeLauncher {
    private static final int MAX_DATA_NODE = 2;
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
            DataNodeImpl dataNodeServant = new DataNodeImpl(Integer.parseInt(args[4]));
            // DataNodeImpl dataNodeServant = new DataNodeImpl(1);


            // export
            org.omg.CORBA.Object ref = rootpoa.servant_to_reference(dataNodeServant);
            DataNode href = DataNodeHelper.narrow(ref);

            // Naming context
            org.omg.CORBA.Object objRef = orb.resolve_initial_references("NameService");
            NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef) ;

            NameComponent[] path = ncRef.to_name("DataNode"+args[4]);
            System.out.println("DataNode" + args[4] + " is ready and waiting...");
            ncRef.rebind(path, href);

            // waiting
            orb.run();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}

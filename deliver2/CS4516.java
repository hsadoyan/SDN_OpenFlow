package net.floodlightcontroller.cs4516;

/**
 * Created by ftlc on 4/2/17.
 */

import java.util.*;

import net.floodlightcontroller.core.*;
import net.floodlightcontroller.packet.IPv4;
import net.floodlightcontroller.packet.TCP;
import org.projectfloodlight.openflow.protocol.OFMessage;
import org.projectfloodlight.openflow.protocol.OFPacketOut;
import org.projectfloodlight.openflow.protocol.OFType;
import org.projectfloodlight.openflow.protocol.action.OFAction;
import org.projectfloodlight.openflow.types.*;

import net.floodlightcontroller.core.module.FloodlightModuleContext;
import net.floodlightcontroller.core.module.FloodlightModuleException;
import net.floodlightcontroller.core.module.IFloodlightModule;
import net.floodlightcontroller.core.module.IFloodlightService;


import java.util.concurrent.ConcurrentSkipListSet;

import net.floodlightcontroller.packet.Ethernet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CS4516 implements IOFMessageListener, IFloodlightModule {

    protected final MacAddress SWITCH_MAC = MacAddress.of("52:54:00:45:16:1A");
    protected IFloodlightProviderService floodlightProvider;
    protected static Logger logger;
    protected HashMap<IPv4Address, MacAddress> ipTable;

    @Override
    public String getName() {
        return CS4516.class.getSimpleName();
    }

    @Override
    public boolean isCallbackOrderingPrereq(OFType type, String name) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isCallbackOrderingPostreq(OFType type, String name) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public Collection<Class<? extends IFloodlightService>> getModuleServices() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Map<Class<? extends IFloodlightService>, IFloodlightService> getServiceImpls() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Collection<Class<? extends IFloodlightService>> getModuleDependencies() {
        Collection<Class<? extends IFloodlightService>> l =
                new ArrayList<Class<? extends IFloodlightService>>();
        l.add(IFloodlightProviderService.class);
        return l;
    }

    @Override
    public void init(FloodlightModuleContext context)
            throws FloodlightModuleException {
        ipTable = new HashMap<>();
        ipTable.put(IPv4Address.of("10.45.7.1"), MacAddress.of("52:54:00:45:16:19"));
        ipTable.put(IPv4Address.of("10.45.7.2"), MacAddress.of("52:54:00:45:16:1A"));
        ipTable.put(IPv4Address.of("10.45.7.3"), MacAddress.of("52:54:00:45:16:1B"));
        ipTable.put(IPv4Address.of("10.45.7.4"), MacAddress.of("52:54:00:45:16:1C"));

        ipTable.put(IPv4Address.of("10.45.7.129"), MacAddress.of("52:54:00:45:16:1B"));
        //Host 2 Aliases
        ipTable.put(IPv4Address.of("10.45.7.34"), MacAddress.of("52:54:00:45:16:1A"));
        ipTable.put(IPv4Address.of("10.45.7.65"), MacAddress.of("52:54:00:45:16:1A"));
        ipTable.put(IPv4Address.of("10.45.7.97"), MacAddress.of("52:54:00:45:16:1A"));
        floodlightProvider = context.getServiceImpl(IFloodlightProviderService.class);

        logger = LoggerFactory.getLogger(CS4516.class);
    }

    @Override
    public void startUp(FloodlightModuleContext context) {
        floodlightProvider.addOFMessageListener(OFType.PACKET_IN, this);
    }

    @Override
    public Command receive(IOFSwitch sw, OFMessage msg, FloodlightContext cntx) {

        if(msg.getType() == OFType.PACKET_IN) {
            Ethernet eth = IFloodlightProviderService.bcStore.get(cntx, IFloodlightProviderService.CONTEXT_PI_PAYLOAD);

            MacAddress srcMac = eth.getSourceMACAddress();
            MacAddress dstMac = eth.getDestinationMACAddress();

            if(eth.getEtherType() != EthType.IPv4){
                return Command.CONTINUE;
            }

            IPv4 ipv4 = (IPv4) eth.getPayload();
            IPv4Address srcIP = ipv4.getSourceAddress();
            IPv4Address dstIP = ipv4.getDestinationAddress();

            System.out.println("Source IP: " +  srcIP.toString());
            System.out.println("Dest IP: " +  dstIP.toString());


            System.out.println("Source MAC: " +  srcMac.toString());
            System.out.println("Dest MAC: " +  dstMac.toString());
            dstMac = ipTable.get(dstIP);
            srcMac = SWITCH_MAC;

            //System.out.println("TCP Payload: " + tcp.toString());

            System.out.println("New Source MAC: " +  srcMac.toString());
            if(!dstMac.equals(null)){
                System.out.println("New Dest MAC: " +  dstMac.toString());
            }

            byte[] serializedData = eth.serialize();
            OFPacketOut po = sw.getOFFactory().buildPacketOut() /* mySwitch is some IOFSwitch object */
                    .setData(serializedData)
                    .setActions(Collections.singletonList((OFAction) sw.getOFFactory().actions().output(OFPort.NORMAL, 1)))
                    .setInPort(OFPort.CONTROLLER)
                    .build();

            sw.write(po);



        }


        return Command.CONTINUE;
    }

}
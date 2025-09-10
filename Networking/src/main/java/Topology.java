import java.util.ArrayList;


record ClientNode(String ipAddress, int port){}
/**
 * The main architecture of the networking module.
 * Implement the interface abstractController and its functions
 */
public class Topology implements AbstractTopology, AbstractController {
    /*
     TODO: Create and remove clusters as required
     TODO: Add and move clients from one cluster to another
    */
    /**
     * The number of clusters in the current network.
     *
     */
    private ArrayList<ArrayList<String>> _destinationIP;
    private ArrayList<ArrayList<Integer>> _destinationPort;
    private ArrayList<ClientNode> _clusters;
    private int _numClusters = 0;
    private int _numClients = 0;


    public Topology() {

    }

    @Override
    public ClientNode GetServer(String dest) {
        for (int i = 0; i < _destinationIP.size(); i++) {
            ArrayList<String> clusterClients = _destinationIP.get(i);
            int idx = clusterClients.indexOf(dest);
            if(idx == -1){
                // Debug write the destination not found
                System.out.println("Destination ip address not found");
            }
            return _clusters.get(idx);
        }
        return null;
    }

    @Override
    public void addUser(String ip, Integer port) {
        _numClients += 1;
    }
}

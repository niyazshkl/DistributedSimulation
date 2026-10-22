package server;

import util.annotations.Tags;
import util.tags.DistributedTags;

@Tags({DistributedTags.SERVER, DistributedTags.RMI})
public class Server {
    public static void main(String[] args){
        RMIServer.main(args);
    }
}

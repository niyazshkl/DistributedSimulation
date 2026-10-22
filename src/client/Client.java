package client;

import util.annotations.Tags;
import util.tags.DistributedTags;

@Tags({DistributedTags.CLIENT, DistributedTags.RMI})
public class Client {
    public static void main(String[] args){
        RMIClient.main(args);
    }
}

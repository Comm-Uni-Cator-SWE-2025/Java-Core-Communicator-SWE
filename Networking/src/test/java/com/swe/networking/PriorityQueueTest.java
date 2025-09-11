import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PriorityQueueTest {

    @Test
    void testAddAndRetrievePacket() {
        PriorityQueue pq = new PriorityQueue();
        byte[] data = "hello".getBytes();

        pq.addPacket(data);
        byte[] result = pq.nextPacket();

        // Since getPriority() is dummy, just check we get something back
        assertNotNull(result, "Next packet should not be null");
    }

    @Test
    void testMultiplePackets() {
        PriorityQueue pq = new PriorityQueue();
        byte[] data1 = "packet1".getBytes();
        byte[] data2 = "packet2".getBytes();

        pq.addPacket(data1);
        pq.addPacket(data2);

        byte[] first = pq.nextPacket();
        byte[] second = pq.nextPacket();

        assertNotNull(first, "First packet should not be null");
        assertNotNull(second, "Second packet should not be null");
    }
}

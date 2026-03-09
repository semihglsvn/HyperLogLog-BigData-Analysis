package hyperLogLog;

public interface HashProvider {
    int hash(String data);
}
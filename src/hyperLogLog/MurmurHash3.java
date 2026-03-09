package hyperLogLog;

public class MurmurHash3 implements HashProvider {
    @Override
    public int hash(String data) {
        byte[] bytes = data.getBytes();
        int c1 = 0xcc9e2d51;
        int c2 = 0x1b873593;
        int h1 = 0; // Başlangıç seed değeri

        int length = bytes.length;
        int roundedEnd = (length & 0xfffffffc); // 4 baytlık bloklara yuvarla

        // Veriyi 4 baytlık bloklar halinde işle
        for (int i = 0; i < roundedEnd; i += 4) {
            int k1 = (bytes[i] & 0xff) | ((bytes[i + 1] & 0xff) << 8) | 
                     ((bytes[i + 2] & 0xff) << 16) | (bytes[i + 3] << 24);
            k1 *= c1;
            k1 = Integer.rotateLeft(k1, 15);
            k1 *= c2;

            h1 ^= k1;
            h1 = Integer.rotateLeft(h1, 13);
            h1 = h1 * 5 + 0xe6546b64;
        }

        // Kalan (tail) baytları işle
        int k1 = 0;
        switch (length & 0x03) {
            case 3:
                k1 = (bytes[roundedEnd + 2] & 0xff) << 16;
            case 2:
                k1 |= (bytes[roundedEnd + 1] & 0xff) << 8;
            case 1:
                k1 |= (bytes[roundedEnd] & 0xff);
                k1 *= c1;
                k1 = Integer.rotateLeft(k1, 15);
                k1 *= c2;
                h1 ^= k1;
        }

        // Son karıştırma (Finalization)
        h1 ^= length;
        h1 ^= h1 >>> 16;
        h1 *= 0x85ebca6b;
        h1 ^= h1 >>> 13;
        h1 *= 0xc2b2ae35;
        h1 ^= h1 >>> 16;

        return h1;
    }
}
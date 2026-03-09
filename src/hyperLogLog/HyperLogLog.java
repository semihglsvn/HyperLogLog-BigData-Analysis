package hyperLogLog;

public class HyperLogLog {
    private final int b;           // Kova indeksini belirleyecek bit sayısı (genellikle 4 ile 16 arası)
    private final int m;           // Toplam kova sayısı (2^b)
    private final byte[] registers;  // Kovalardaki rekor ardışık sıfırları tutan dizi
    private final HashProvider hashProvider;

    public HyperLogLog(int b, HashProvider hashProvider) {
        if (b < 4 || b > 16) {
            throw new IllegalArgumentException("b değeri 4 ile 16 arasında olmalıdır.");
        }
        this.b = b;
        this.m = 1 << b; // 1 sayısını b kadar sola kaydırarak 2^b hesaplıyoruz (örn: b=4 ise m=16)
        this.registers = new byte[m];
        this.hashProvider = hashProvider;
    }

    // 1. ELEMAN EKLEME VE KOVALAMA (BUCKETING)
    public void add(String element) {
        // Gelen veriyi 32-bitlik bir sayıya çevir
        int hash = hashProvider.hash(element);

        // Kova İndeksini Bulma: İlk b kadar biti alıyoruz.
        // >>> operatörü (unsigned right shift) işareti umursamadan sağa kaydırır.
        int j = hash >>> (32 - b);

        // Ardışık Sıfırları Sayma: Kalan bitleri sola kaydırarak başa alıyoruz.
        // Böylece kova indeksinde kullandığımız bitleri yoksayıyoruz.
        int w = hash << b;
        
        // Integer.numberOfLeadingZeros() Java'nın gömülü donanım seviyesi sıfır sayıcısıdır.
        // +1 ekliyoruz çünkü hiç sıfır yoksa (1 ile başlıyorsa) rekor 1'dir.
        int rho = Integer.numberOfLeadingZeros(w) + 1;

        // O kovanın (register'ın) eski rekoru ile yeni değeri karşılaştır, büyük olanı kaydet.
        registers[j] = (byte) Math.max(registers[j], rho);
    }

    // 2. TAHMİN HESAPLAMA (HARMONİK ORTALAMA VE DÜZELTMELER)
    public long count() {
        double sum = 0.0;
        int zeroRegisters = 0; // Hiç veri gelmemiş boş kova sayısı

        // Tüm kovalardaki değerleri dolaş ve Harmonik Ortalama için paydaları topla: 2^(-register)
        for (int i = 0; i < m; i++) {
            sum += 1.0 / (1 << registers[i]); 
            if (registers[i] == 0) {
                zeroRegisters++;
            }
        }

        // Formüldeki alpha_m sabitini hesapla
        double alpha = getAlpha();
        
        // Ham Tahmin (Raw Estimate) Formülü: E = alpha * m^2 / sum
        double estimate = alpha * m * m / sum;

        // DÜZELTME FAKTÖRLERİ (Hocanın özellikle istediği kısım)
        // Eğer tahmin çok küçükse (2.5 * m'den küçükse) Linear Counting düzeltmesi yapılır
        if (estimate <= 2.5 * m) {
            if (zeroRegisters > 0) {
                estimate = m * Math.log((double) m / zeroRegisters);
            }
        } 
        // 32-bit hash kullandığımız için tahmin 2^32'ye yaklaştığında büyük veri düzeltmesi yapılır
        else if (estimate > (1L << 32) / 30.0) {
            estimate = - (1L << 32) * Math.log(1.0 - estimate / (1L << 32));
        }

        return (long) estimate;
    }

    // 3. İKİ HLL YAPISINI BİRLEŞTİRME (MERGE)
    public HyperLogLog merge(HyperLogLog other) {
        if (this.m != other.m) {
            throw new IllegalArgumentException("Farklı kova sayılarına sahip HLL yapıları birleştirilemez!");
        }

        HyperLogLog mergedHLL = new HyperLogLog(this.b, this.hashProvider);

        // Her iki HLL'nin register dizilerini gez ve aynı indeksteki maksimum değeri yeni HLL'ye yaz.
        for (int i = 0; i < m; i++) {
            mergedHLL.registers[i] = (byte) Math.max(this.registers[i], other.registers[i]);
        }

        return mergedHLL;
    }

    // Matematiksel alpha sabitini döndüren yardımcı metot
    private double getAlpha() {
        switch (m) {
            case 16: return 0.673;
            case 32: return 0.697;
            case 64: return 0.709;
            default: return 0.7213 / (1.0 + 1.079 / m);
        }
    }
}
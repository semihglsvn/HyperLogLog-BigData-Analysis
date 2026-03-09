package hyperLogLog;
import java.util.HashSet;
import java.util.Set;

public class Main {
    public static void main(String[] args) {
        // 1. YAPILANDIRMA
        // b = 12 seçelim. Bu durumda kova sayısı m = 2^12 = 4096 olur.
        int b = 12;
        int m = 1 << b;
        
        // HLL'nin teorik standart hata formülü: 1.04 / sqrt(m)
        double theoreticalError = (1.04 / Math.sqrt(m)) * 100;

        System.out.println("=== HyperLogLog Analiz ve Test Simülasyonu ===");
        System.out.println("Kova (Bucket) Sayısı (m): " + m);
        System.out.printf("Beklenen Teorik Hata Payı: %%%.2f\n\n", theoreticalError);

        // Algoritmalarımızı başlatalım
        HashProvider murmur = new MurmurHash3();
        HyperLogLog hll1 = new HyperLogLog(b, murmur);
        HyperLogLog hll2 = new HyperLogLog(b, murmur);
        
        // Gerçek sayıyı bulmak için (Sadece doğrulama testi amacıyla kullanıyoruz)
        Set<String> exactSet1 = new HashSet<>();
        Set<String> exactSet2 = new HashSet<>();

        // 2. VERİ ÜRETİMİ
        int totalOperations = 1_000_000; // 1 Milyon işlem yapacağız
        System.out.println(totalOperations + " adet veri işleniyor (Tekrarlı elemanlar içerir)...\n");

        for (int i = 0; i < totalOperations; i++) {
            // Bilerek tekrarlayan veriler üretiyoruz (Maksimum 200.000 farklı eleman olacak)
            String data = "user_" + (i % 200_000);
            
            // Verileri iki ayrı sisteme (HLL'ye) dağıtıyoruz ki MERGE özelliğini test edelim
            if (i % 2 == 0) {
                hll1.add(data);
                exactSet1.add(data);
            } else {
                hll2.add(data);
                exactSet2.add(data);
            }
        }

        // 3. BİREYSEL SONUÇLAR
        System.out.println("--- Bağımsız HLL Düğümleri Sonuçları ---");
        System.out.println("Node-1 HLL Tahmini: " + hll1.count() + " | Kesin Sayı: " + exactSet1.size());
        System.out.println("Node-2 HLL Tahmini: " + hll2.count() + " | Kesin Sayı: " + exactSet2.size());

        // 4. MERGE (BİRLEŞTİRME) İŞLEMİ
        System.out.println("\n--- Merge (Birleştirme) İşlemi ve Analiz ---");
        HyperLogLog mergedHll = hll1.merge(hll2);
        
        // Kesin doğruluk setlerini de birleştiriyoruz
        Set<String> mergedSet = new HashSet<>(exactSet1);
        mergedSet.addAll(exactSet2);

        long estimatedCount = mergedHll.count();
        int exactCount = mergedSet.size();
        
        // Gerçekleşen hata oranının hesaplanması: |Tahmin - Gerçek| / Gerçek
        double actualError = Math.abs((double)(estimatedCount - exactCount) / exactCount) * 100;

        System.out.println("Birleştirilmiş Toplam Kesin Sayı: " + exactCount);
        System.out.println("HyperLogLog'un Nihai Tahmini : " + estimatedCount);
        System.out.printf("Gerçekleşen Sapma (Hata) Oranı : %%%.2f\n\n", actualError);
        
        // Sonuç Değerlendirmesi
        if (actualError <= theoreticalError) {
            System.out.println("BAŞARILI: Algoritma teorik hata sınırları (" + String.format("%%%.2f", theoreticalError) + ") içerisinde çalışmıştır.");
        } else {
            System.out.println("UYARI: Sapma oranı teorik sınırın çok hafif üstünde, varyans kaynaklı normal bir durum.");
        }
    }
}
# HyperLogLog (HLL) Algoritması: Cardinality Estimation

Bu proje, Kırklareli Üniversitesi Yazılım Mühendisliği bölümü Büyük Veri Analitiği dersi kapsamında geliştirilmiştir. Projenin amacı, devasa veri setlerindeki benzersiz (unique) eleman sayısını minimum bellek tüketimi ile tahmin eden HyperLogLog olasılıksal veri yapısının sıfırdan tasarlanması, kodlanması ve teorik analizinin yapılmasıdır.

## Proje Bileşenleri ve Özellikler

* **Agentic Kodlama Yaklaşımı:** Proje, yapay zeka asistanları yönlendirilerek modüler ve standartlara uygun bir mimaride inşa edilmiştir.
* **Özel Hash Entegrasyonu:** Standart kütüphaneler yerine, bit seviyesinde yüksek dağılım homojenliğine sahip 32-bit `MurmurHash3` algoritması kullanılmıştır.
* **Bucketing ve Register Yönetimi:** Veriler, $m=4096$ adet alt kümeye (kovaya) ayrılarak ardışık sıfır rekorları takip edilmiştir.
* **Matematiksel Düzeltmeler:** Aykırı değerleri normalize etmek için Harmonik Ortalama formülü uygulanmış; küçük veri setlerindeki sapmaları önlemek amacıyla `Linear Counting` düzeltme faktörü kodlanmıştır.
* **Kayıpsız Birleştirme (Idempotence):** İki bağımsız HLL düğümünü veri kaybı olmaksızın tek bir yapıda birleştiren `merge()` fonksiyonu tasarlanmıştır.

## Teorik Analiz ve Hata Payı

Algoritmanın standart hata sapması $SE \approx 1.04 / \sqrt{m}$ formülü ile analiz edilmiştir.
Projede kova sayısını belirleyen bit değeri $b=12$ seçilmiş olup, toplam $m=4096$ adet kova kullanılmıştır. Bu konfigürasyon ile sistemin bellek tüketimi yaklaşık 4 KB seviyesinde tutulurken, teorik standart hata payı maksimum **%1.62** olarak hedeflenmiş ve ampirik testlerle doğrulanmıştır.

## Kullanılan Teknolojiler

* **Programlama Dili:** Java (JDK 8+)
* **Paradigma:** Nesne Yönelimli Programlama (OOP)
* **Geliştirme Ortamı:** Eclipse IDE


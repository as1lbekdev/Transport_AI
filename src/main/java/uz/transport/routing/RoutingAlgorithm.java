package uz.transport.routing;

/** Marshrut algoritmi interfeysi. Yangi algoritm qo'shish uchun shuni implement qilish yetarli. */
public interface RoutingAlgorithm {

    /** API'da algorithm= parametri sifatida ishlatiladigan nom. */
    String name();

    /**
     * @param start        boshlang'ich tugun
     * @param goal         manzil tuguni
     * @param trafficAware true bo'lsa, og'irlik tirbandlikni hisobga oladi
     */
    SearchResult search(int start, int goal, boolean trafficAware);
}

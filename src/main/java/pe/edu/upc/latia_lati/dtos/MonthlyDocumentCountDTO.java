package pe.edu.upc.latia_lati.dtos;

public class MonthlyDocumentCountDTO {
    private int year;
    private int month;
    private long total;

    public MonthlyDocumentCountDTO() {}

    public MonthlyDocumentCountDTO(int year, int month, long total) {
        this.year = year;
        this.month = month;
        this.total = total;
    }

    public int getYear() { return year; }
    public void setYear(int year) { this.year = year; }
    public int getMonth() { return month; }
    public void setMonth(int month) { this.month = month; }
    public long getTotal() { return total; }
    public void setTotal(long total) { this.total = total; }
}

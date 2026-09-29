# JDBC Repository, Transactions & Security Documentation

## 1. SQL Injection Prevention Proof

- **Đầu vào thử nghiệm**: Truyền chuỗi độc hại `' OR '1'='1` vào phương thức tìm kiếm `findByCustomer`.
- **Kết quả**: Do sử dụng `PreparedStatement` với tham số ràng buộc `?`, PostgreSQL coi toàn bộ chuỗi này là giá trị literal của `customer_id` chứ không biên dịch thành mã SQL thực thi. Kết quả trả về danh sách rỗng (`0` bản ghi), hoàn toàn ngăn chặn rò rỉ dữ liệu toàn bảng.

## 2. Connection Pool Exhaustion & Leak Demonstration

- **Cấu hình kiểm nghiệm**: HikariCP với `maximumPoolSize = 5` và `connectionTimeout = 2000ms`.
- **Thực nghiệm rò rỉ**: Khi bỏ `try-with-resources` ở `Connection` và gọi phương thức bị lỗi liên tục 100 lần, các kết nối không được hoàn trả về pool.
- **Kết quả ghi nhận**: Ứng dụng bị treo (hang) và ném ra ngoại lệ timeout sau khi hết 5 kết nối:
  ```text
  java.sql.SQLTransientConnectionException: HikariPool-1 - Connection is not available, request timed out after 2000ms.
  ```

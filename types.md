# Type Selection Justification

- **SKU (`String`)**: Chọn `String` vì mã SKU chứa cả ký tự chữ và số (ví dụ `KB-01`). Loại bỏ `char[]` vì bất tiện khi xử lý chuỗi và loại bỏ kiểu số nguyên vì SKU không tham gia tính toán số học.
- **Quantity (`long` hoặc `int`)**: Chọn `long` vì số lượng đơn vị luôn là số nguyên không âm và `long` tránh hiện tượng tràn số khi dữ liệu lớn. Loại bỏ `float`/`double` vì số lượng hàng hoá rời rạc không thể là số thực thập phân.
- **Unit Price & Total Amount (`BigDecimal`)**: Chọn `BigDecimal(String)` để tính toán tiền tệ chính xác tuyệt đối. Loại bỏ hoàn toàn `double`/`float` vì số thực dấu phẩy động (IEEE 754) gây ra sai số làm tròn nhị phân (binary floating-point rounding error), không được phép dùng cho tài chính.
- **Summary Result (`record Summary`)**: Chọn `record` của Java 21 vì đây là cấu trúc bất biến (immutable data carrier), gọn gàng, tự sinh constructor, getter, `equals()`, `hashCode()` và `toString()`.

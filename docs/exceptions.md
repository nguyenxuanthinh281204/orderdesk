# Exception Boundaries and Failures

## 1. Exception Classification

| Failure Scenario                      | Exception Class            | Kind                               | Justification                                                                                                                                                   |
| :------------------------------------ | :------------------------- | :--------------------------------- | :-------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| **Catalog File Missing / Unreadable** | `CatalogLoadException`     | **Checked** (`Exception`)          | Lỗi ngoại vi/I/O có thể phục hồi (caller có thể chuyển hướng tải file backup, chờ người dùng chọn lại đường dẫn hoặc cấu hình lại).                             |
| **Malformed Product CSV Row**         | `CatalogParseException`    | **Unchecked** (`RuntimeException`) | Dữ liệu catalog nội bộ không đúng định dạng (bug hoặc file dữ liệu bị hỏng), hệ thống không thể tự sửa lúc runtime mà lập trình viên/vận hành phải sửa dữ liệu. |
| **Order Line with Unknown SKU**       | `UnknownSkuException`      | **Unchecked** (`RuntimeException`) | Lỗi vi phạm nghiệp vụ khi người dùng đặt một mã hàng không hề tồn tại trong danh mục sản phẩm.                                                                  |
| **Product is Inactive**               | `InactiveProductException` | **Unchecked** (`RuntimeException`) | Sản phẩm có tồn tại nhưng đã ngừng kinh doanh/bị khóa; hệ thống từ chối tạo đơn.                                                                                |

---

## 2. Real Stack Trace Analysis

```text
com.fsa.orderdesk.catalog.CatalogLoadException: Failed to load catalog from: /path/to/missing_catalog.csv
	at com.fsa.orderdesk.catalog.CatalogLoader.loadFromPath(CatalogLoader.java:31)
	at com.fsa.orderdesk.catalog.CatalogLoaderTest.testIOExceptionPreservedAsCause(CatalogLoaderTest.java:42)
	at java.base/jdk.internal.reflect.DirectMethodHandleAccessor.invoke(DirectMethodHandleAccessor.java:103)
	at java.base/java.lang.reflect.Method.invoke(Method.java:580)
	at org.junit.platform.commons.util.ReflectionUtils.invokeMethod(ReflectionUtils.java:728)
	at org.junit.jupiter.engine.execution.MethodInvocation.proceed(MethodInvocation.java:60)
Caused by: java.nio.file.NoSuchFileException: /path/to/missing_catalog.csv
	at java.base/sun.nio.fs.WindowsException.translateToIOException(WindowsException.java:85)
	at java.base/sun.nio.fs.WindowsException.rethrowAsIOException(WindowsException.java:103)
	at java.base/sun.nio.fs.WindowsFileSystemProvider.newByteChannel(WindowsFileSystemProvider.java:234)
	at java.base/java.nio.file.Files.newByteChannel(Files.java:379)
	at java.base/java.nio.file.Files.newInputStream(Files.java:422)
	at java.base/java.nio.file.Files.newBufferedReader(Files.java:2910)
	at com.fsa.orderdesk.catalog.CatalogLoader.loadFromPath(CatalogLoader.java:25)
	... 5 more
```

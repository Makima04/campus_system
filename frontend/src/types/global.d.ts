/**
 * 全局类型声明
 */

// spark-md5模块声明
declare module 'spark-md5' {
  interface SparkMD5 {
    append(data: ArrayBuffer): void;
    reset(): void;
    end(): string;
  }

  interface SparkMD5Constructor {
    new(): SparkMD5;
    ArrayBuffer: {
      new(): SparkMD5;
    }
  }

  const sparkMD5: SparkMD5Constructor;
  export default sparkMD5;
} 
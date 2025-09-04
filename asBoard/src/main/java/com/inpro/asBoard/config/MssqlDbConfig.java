//package com.inpro.asBoard.config;
//
//import org.apache.ibatis.session.SqlSessionFactory;
//import org.mybatis.spring.SqlSessionFactoryBean;
//import org.mybatis.spring.SqlSessionTemplate;
//import org.mybatis.spring.annotation.MapperScan;
//import org.springframework.beans.factory.annotation.Qualifier;
//import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
//import org.springframework.boot.context.properties.ConfigurationProperties;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.context.annotation.Primary;
//import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
//
//import javax.sql.DataSource;
//
//@Configuration
//@MapperScan(
//        basePackages = {
//                "com.inpro.asBoard.as.mapper",
//                "com.inpro.asBoard.user"
//        },  // MSSQL용 Mapper 경로
//        sqlSessionFactoryRef = "mssqlSqlSessionFactory"
//)
//public class MssqlDbConfig {
//
//    @Bean
//    @ConfigurationProperties("spring.datasource")  // ✅ application.yml의 기본 MSSQL 설정
//    public DataSourceProperties mssqlDataSourceProperties() {
//        return new DataSourceProperties();
//    }
//
//    @Primary
//    @Bean(name = "mssqlDataSource")
//    public DataSource mssqlDataSource(@Qualifier("mssqlDataSourceProperties") DataSourceProperties properties) {
//        return properties.initializeDataSourceBuilder().build();
//    }
//
//    @Bean(name = "mssqlSqlSessionFactory")
//    public SqlSessionFactory mssqlSqlSessionFactory(@Qualifier("mssqlDataSource") DataSource mssqlDataSource) throws Exception {
//        SqlSessionFactoryBean factory = new SqlSessionFactoryBean();
//        factory.setDataSource(mssqlDataSource);
//
//        // ✅ XML 매퍼 위치
//        factory.setMapperLocations(new PathMatchingResourcePatternResolver()
//                .getResources("classpath:/mapper/**/*.xml"));
//
//        // ✅ DTO alias 패키지 설정
//        factory.setTypeAliasesPackage("com.inpro.asBoard");
//
//        // ✅ mapUnderscoreToCamelCase 등 MyBatis 설정 적용
//        org.apache.ibatis.session.Configuration mybatisConfig = new org.apache.ibatis.session.Configuration();
//        mybatisConfig.setMapUnderscoreToCamelCase(true); // 스네이크 → 카멜 매핑 적용
//        factory.setConfiguration(mybatisConfig);
//
//        return factory.getObject();
//    }
//
//    @Bean(name = "mssqlSqlSessionTemplate")
//    public SqlSessionTemplate mssqlSqlSessionTemplate(@Qualifier("mssqlSqlSessionFactory") SqlSessionFactory factory) {
//        return new SqlSessionTemplate(factory);
//    }
//}
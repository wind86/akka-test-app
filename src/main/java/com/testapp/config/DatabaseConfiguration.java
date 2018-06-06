package com.testapp.config;

import static java.util.Collections.singletonList;
import static org.apache.commons.lang3.ArrayUtils.toArray;
import static org.cassandraunit.utils.EmbeddedCassandraServerHelper.cleanEmbeddedCassandra;
import static org.cassandraunit.utils.EmbeddedCassandraServerHelper.startEmbeddedCassandra;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Date;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.apache.cassandra.exceptions.ConfigurationException;
import org.apache.thrift.transport.TTransportException;
import org.cassandraunit.spring.EmbeddedCassandra;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cassandra.config.ClusterBuilderConfigurer;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.cassandra.config.SchemaAction;
import org.springframework.data.cassandra.config.java.AbstractCassandraConfiguration;
import org.springframework.data.cassandra.repository.config.EnableCassandraRepositories;

import com.datastax.driver.core.DataType;
import com.datastax.driver.core.LocalDate;
import com.datastax.driver.core.ProtocolVersion;
import com.datastax.driver.core.TypeCodec;
import com.datastax.driver.core.exceptions.InvalidTypeException;

@Configuration
@EmbeddedCassandra
@EnableCassandraRepositories("com.testapp.repository")
public class DatabaseConfiguration extends AbstractCassandraConfiguration {

  private static final String CREATE_KEYSPACE_QUERY = ""
      + "CREATE KEYSPACE IF NOT EXISTS %s "
      + "WITH durable_writes = true "
      + "AND replication = {'class':'SimpleStrategy', 'replication_factor' : 3};";

  private static final String DROP_KEYSPACE_QUERY = "DROP KEYSPACE %s;";

  private static final String PROPERTIES_FILE = "cassandra.yaml";

  private static final long STARTUP_TIMEOUT = 40000;


  @Value("${spring.data.cassandra.keyspace-name}")
  private String keyspace;

  @Value("${spring.data.cassandra.contact-points}")
  private String contactPoints;

  @Value("${spring.data.cassandra.port}")
  private Integer port;

  public DatabaseConfiguration() {
    System.setProperty("cassandra.jmx.local.port", "9242");
    System.setProperty("com.datastax.driver.EXTENDED_PEER_CHECK", "false");
  }

  @PostConstruct
  public void startDatabase() throws ConfigurationException, TTransportException, IOException {
    startEmbeddedCassandra(PROPERTIES_FILE, STARTUP_TIMEOUT);
  }

  @PreDestroy
  public void stopDatabase() {
    cleanEmbeddedCassandra();
  }

  @Override
  public SchemaAction getSchemaAction() {
    return SchemaAction.CREATE_IF_NOT_EXISTS;
  }

  @Override
  public String[] getEntityBasePackages() {
    return toArray("com.testapp.entity");
  }

  @Override
  protected String getKeyspaceName() {
    return keyspace;
  }

  @Override
  protected List<String> getStartupScripts() {
    return createKeyspaceScript(CREATE_KEYSPACE_QUERY);
  }

  @Override
  protected List<String> getShutdownScripts() {
    return createKeyspaceScript(DROP_KEYSPACE_QUERY);
  }

  @Override
  protected ClusterBuilderConfigurer getClusterBuilderConfigurer() {
    return clusterBuilder -> {
      clusterBuilder.getConfiguration().getCodecRegistry().register(new DateTimeCodec());
      clusterBuilder.withoutJMXReporting();
      clusterBuilder.withoutMetrics();
      return clusterBuilder;
    };
  }

  private List<String> createKeyspaceScript(final String queryPattern) {
    return singletonList(createKeyspaceQuery(queryPattern));
  }

  private String createKeyspaceQuery(final String queryPattern) {
    return String.format(queryPattern, keyspace);
  }

  private static class DateTimeCodec extends TypeCodec<LocalDate> {

    public DateTimeCodec() {
      super(DataType.timestamp(), LocalDate.class);
    }

    @Override
    public ByteBuffer serialize(final LocalDate value, final ProtocolVersion protocolVersion) throws InvalidTypeException {
      return value == null ? null : TypeCodec.bigint().serializeNoBoxing(value.getMillisSinceEpoch(), protocolVersion);
    }

    @Override
    public LocalDate deserialize(final ByteBuffer bytes, final ProtocolVersion protocolVersion) throws InvalidTypeException {
      return bytes == null || bytes.remaining() == 0 ? null: LocalDate.fromMillisSinceEpoch(TypeCodec.bigint().deserializeNoBoxing(bytes, protocolVersion));
    }

    @SuppressWarnings("deprecation")
    @Override
    public LocalDate parse(final String value) throws InvalidTypeException {
      if (value == null || value.equals("NULL")) {
        return null;
      }

      try {
        return LocalDate.fromMillisSinceEpoch(Date.parse(value));
      } catch (final IllegalArgumentException iae) {
        throw new InvalidTypeException("Could not parse format: " + value, iae);
      }
    }

    @Override
    public String format(final LocalDate value) throws InvalidTypeException {
      if (value == null) {
        return "NULL";
      }

      return Long.toString(value.getMillisSinceEpoch());
    }
  }
}
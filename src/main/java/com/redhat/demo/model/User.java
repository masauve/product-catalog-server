package com.redhat.demo.model;

import static org.wildfly.security.password.interfaces.BCryptPassword.ALGORITHM_BCRYPT;
import static org.wildfly.security.password.interfaces.BCryptPassword.BCRYPT_SALT_SIZE;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.concurrent.ThreadLocalRandom;

import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wildfly.security.password.PasswordFactory;
import org.wildfly.security.password.interfaces.BCryptPassword;
import org.wildfly.security.password.spec.EncryptablePasswordSpec;
import org.wildfly.security.password.spec.IteratedSaltedPasswordAlgorithmSpec;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;

@Entity
@Table(name = "users")
public class User extends PanacheEntityBase {

    private static final Logger log = LoggerFactory.getLogger("User");

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Integer id;

    @Column(length = 100)
    public String email;

    @Column(name = "password_hash", length = 100)
    public String passwordHash;

    @Column(length = 100)
    public String salt;

    @Column(name = "iteration_count")
    public Integer iterations;

    @Column(name = "created_at")
    public LocalDateTime createdAt;

    public User() {

    }

    public User(final Integer id, final String email, final String passwordHash, final String salt,
            final Integer iterations, final LocalDateTime createdAt) {
        this.id = id;
        this.email = email;
        this.passwordHash = passwordHash;
        this.salt = salt;
        this.iterations = iterations;
        this.createdAt = createdAt;
    }

    public void setPasswordHash(final String password) {
        final byte[] salt = generateRandomSalt(BCRYPT_SALT_SIZE);
        final int iterationCount = 10;

        try {
            final PasswordFactory factory = PasswordFactory.getInstance(ALGORITHM_BCRYPT);
            final BCryptPassword bCryptPassword = (BCryptPassword) factory.generatePassword(new EncryptablePasswordSpec(
                    password.toCharArray(), new IteratedSaltedPasswordAlgorithmSpec(iterationCount, salt)));
            this.passwordHash = Base64.getEncoder().encodeToString(bCryptPassword.getHash());
            this.salt = Base64.getEncoder().encodeToString(bCryptPassword.getSalt());
            this.iterations = bCryptPassword.getIterationCount();
            log.info("hash: " + this.passwordHash + ", salt:" + this.salt + ", iterations:" + this.iterations);
        } catch (final InvalidKeySpecException e) {
            throw new RuntimeException("Password encryption failed, invalid key spec", e);
        } catch (final NoSuchAlgorithmException e) {
            throw new RuntimeException("Password encryption failed, no such algorithm", e);
        }
    }

    private static byte[] generateRandomSalt(final int saltSize) {
        final byte[] randomSalt = new byte[saltSize];
        ThreadLocalRandom.current().nextBytes(randomSalt);
        return randomSalt;
    }

    @Override
    public String toString() {
        try (Jsonb jsonb = JsonbBuilder.create()) {
            return jsonb.toJson(this);
        } catch (Exception e) {
            throw new RuntimeException("Occurred creating json", e);
        }
    }
}
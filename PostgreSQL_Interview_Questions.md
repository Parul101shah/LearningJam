# PostgreSQL Interview Questions for a Java Developer (2 Years Experience)

---

## Section 1 — Fundamentals

### Q1. What is PostgreSQL and how is it different from MySQL?

| Aspect | PostgreSQL | MySQL |
|---|---|---|
| Type | Object-Relational | Purely Relational |
| ACID Compliance | Full (all storage engines) | Full only with InnoDB |
| JSON Support | Native `jsonb` (binary, indexable) | Basic `JSON` type |
| Advanced Features | CTEs, Window Functions, Materialized Views, Full-Text Search | Fewer advanced features |
| Concurrency Model | MVCC (Multi-Version Concurrency Control) | MVCC (InnoDB only) |
| License | Truly open-source (PostgreSQL License) | GPL (Oracle-owned) |

---

### Q2. What are the main data types in PostgreSQL?

```
Category          Types
─────────────────────────────────────────
Numeric           INTEGER, BIGINT, SMALLINT, SERIAL, NUMERIC, DECIMAL, REAL, DOUBLE PRECISION
Character         CHAR(n), VARCHAR(n), TEXT
Boolean           BOOLEAN
Date/Time         DATE, TIME, TIMESTAMP, TIMESTAMPTZ, INTERVAL
JSON              JSON, JSONB
UUID              UUID
Array             INTEGER[], TEXT[] (any type can be an array)
Binary            BYTEA
Network           INET, CIDR, MACADDR
Special           ENUM, HSTORE (key-value), RANGE types
```

**Follow-up: What's the difference between `JSON` and `JSONB`?**

- `JSON` stores as plain text — preserves formatting, slower to query.
- `JSONB` stores in binary — discards whitespace/key order, but supports indexing and is much faster to query.

```sql
-- JSONB allows you to index and query efficiently
CREATE INDEX idx_data ON orders USING GIN (metadata);
SELECT * FROM orders WHERE metadata @> '{"status": "shipped"}';
```

---

### Q3. What is the difference between `CHAR`, `VARCHAR`, and `TEXT`?

| Type | Behavior |
|---|---|
| `CHAR(n)` | Fixed length — pads with spaces if shorter |
| `VARCHAR(n)` | Variable length — max `n` characters |
| `TEXT` | Variable length — **unlimited** (PostgreSQL-specific, no performance penalty vs `VARCHAR`) |

> **Tip for Java devs:** In PostgreSQL, `TEXT` and `VARCHAR` perform identically. Unlike Oracle/MySQL, there's no reason to avoid `TEXT`.

---

### Q4. What is a Primary Key vs Unique Key?

| Feature | Primary Key | Unique Key |
|---|---|---|
| NULLs allowed? | ❌ No | ✅ Yes (one NULL in most DBs, multiple in PostgreSQL) |
| How many per table? | Only 1 | Multiple |
| Creates index? | Yes (Clustered-like) | Yes (Non-clustered) |
| Purpose | Unique identity of a row | Enforce uniqueness on other columns |

---

### Q5. What are Constraints? Name all types.

```sql
CREATE TABLE employees (
    id          SERIAL PRIMARY KEY,                        -- PRIMARY KEY
    email       VARCHAR(150) NOT NULL UNIQUE,              -- NOT NULL + UNIQUE
    department  VARCHAR(50) DEFAULT 'General',             -- DEFAULT
    salary      NUMERIC CHECK (salary > 0),                -- CHECK
    manager_id  INT REFERENCES employees(id)               -- FOREIGN KEY
    -- EXCLUDE constraint (PostgreSQL-specific, for ranges)
);
```

**All constraint types:**
`PRIMARY KEY`, `FOREIGN KEY`, `UNIQUE`, `NOT NULL`, `CHECK`, `DEFAULT`, `EXCLUDE`

---

## Section 2 — Queries (Most Frequently Asked)

### Q6. Write a query to find the 2nd highest salary.

```sql
-- Method 1: LIMIT + OFFSET
SELECT DISTINCT salary FROM employees ORDER BY salary DESC LIMIT 1 OFFSET 1;

-- Method 2: Subquery
SELECT MAX(salary) FROM employees WHERE salary < (SELECT MAX(salary) FROM employees);

-- Method 3: DENSE_RANK (most flexible — works for Nth highest)
SELECT salary FROM (
    SELECT salary, DENSE_RANK() OVER (ORDER BY salary DESC) AS rnk
    FROM employees
) sub
WHERE rnk = 2;
```

---

### Q7. What is the difference between `WHERE` and `HAVING`?

```sql
-- WHERE filters ROWS (before grouping)
SELECT department, COUNT(*) FROM employees
WHERE salary > 50000
GROUP BY department;

-- HAVING filters GROUPS (after grouping)
SELECT department, COUNT(*) FROM employees
GROUP BY department
HAVING COUNT(*) > 5;
```

| | `WHERE` | `HAVING` |
|---|---|---|
| Filters | Individual rows | Groups |
| Used with | Any query | Only with `GROUP BY` |
| Aggregates allowed? | ❌ No | ✅ Yes |

---

### Q8. Explain all types of JOINs with examples.

```
Table: employees              Table: departments
┌────┬────────┬────────┐     ┌────────┬────────────┐
│ id │  name  │ dept_id│     │ dept_id│  dept_name  │
├────┼────────┼────────┤     ├────────┼────────────┤
│  1 │ Alice  │   10   │     │   10   │ Engineering │
│  2 │ Bob    │   20   │     │   20   │ Marketing   │
│  3 │ Charlie│  NULL  │     │   30   │ Finance     │
└────┴────────┴────────┘     └────────┴────────────┘
```

```sql
-- INNER JOIN (only matching rows)
SELECT e.name, d.dept_name FROM employees e
INNER JOIN departments d ON e.dept_id = d.dept_id;
-- Result: Alice-Engineering, Bob-Marketing

-- LEFT JOIN (all from left + matching from right)
SELECT e.name, d.dept_name FROM employees e
LEFT JOIN departments d ON e.dept_id = d.dept_id;
-- Result: Alice-Engineering, Bob-Marketing, Charlie-NULL

-- RIGHT JOIN (all from right + matching from left)
SELECT e.name, d.dept_name FROM employees e
RIGHT JOIN departments d ON e.dept_id = d.dept_id;
-- Result: Alice-Engineering, Bob-Marketing, NULL-Finance

-- FULL OUTER JOIN (all from both sides)
SELECT e.name, d.dept_name FROM employees e
FULL OUTER JOIN departments d ON e.dept_id = d.dept_id;
-- Result: Alice-Engineering, Bob-Marketing, Charlie-NULL, NULL-Finance

-- CROSS JOIN (cartesian product — every row × every row)
SELECT e.name, d.dept_name FROM employees e CROSS JOIN departments d;
-- Result: 3 × 3 = 9 rows

-- SELF JOIN (table joined with itself)
SELECT e.name AS employee, m.name AS manager
FROM employees e JOIN employees m ON e.manager_id = m.id;
```

---

### Q9. What is the difference between `UNION` and `UNION ALL`?

```sql
-- UNION: combines results and REMOVES duplicates (slower)
SELECT name FROM employees UNION SELECT name FROM contractors;

-- UNION ALL: combines results and KEEPS duplicates (faster)
SELECT name FROM employees UNION ALL SELECT name FROM contractors;
```

> **Rule:** Always use `UNION ALL` unless you specifically need deduplication.

---

### Q10. What are Subqueries? Types?

```sql
-- 1. Scalar Subquery (returns single value)
SELECT name, (SELECT MAX(salary) FROM employees) AS max_sal FROM employees;

-- 2. Row Subquery (returns single row)
SELECT * FROM employees WHERE (dept_id, salary) = (SELECT dept_id, MAX(salary) FROM employees GROUP BY dept_id LIMIT 1);

-- 3. Table Subquery / Derived Table (returns multiple rows)
SELECT * FROM employees WHERE dept_id IN (SELECT dept_id FROM departments WHERE location = 'NYC');

-- 4. Correlated Subquery (references outer query — runs once per row)
SELECT e.name, e.salary FROM employees e
WHERE e.salary > (SELECT AVG(salary) FROM employees WHERE dept_id = e.dept_id);
```

---

### Q11. What are Window Functions? (Very important!)

Window functions perform calculations **across a set of rows related to the current row** — without collapsing rows like `GROUP BY`.

```sql
SELECT
    name,
    department,
    salary,
    RANK()       OVER (ORDER BY salary DESC)                    AS company_rank,
    DENSE_RANK() OVER (PARTITION BY department ORDER BY salary DESC) AS dept_rank,
    ROW_NUMBER() OVER (ORDER BY salary DESC)                    AS row_num,
    SUM(salary)  OVER (PARTITION BY department)                 AS dept_total,
    AVG(salary)  OVER ()                                        AS company_avg,
    LAG(salary)  OVER (ORDER BY salary)                         AS prev_salary,
    LEAD(salary) OVER (ORDER BY salary)                         AS next_salary
FROM employees;
```

**Key difference:**

| | `RANK` | `DENSE_RANK` | `ROW_NUMBER` |
|---|---|---|---|
| Ties | Same rank, **skips** next | Same rank, **no skip** | Always unique |
| Example (salaries: 100, 90, 90, 80) | 1, 2, 2, **4** | 1, 2, 2, **3** | 1, 2, 3, **4** |

---

## Section 3 — Indexes & Performance

### Q12. What are Indexes? Types in PostgreSQL?

An index is a **data structure** that speeds up reads (at the cost of slightly slower writes).

```sql
-- B-Tree (default — best for =, <, >, BETWEEN, ORDER BY)
CREATE INDEX idx_emp_name ON employees(name);

-- Hash (only for = equality)
CREATE INDEX idx_emp_email ON employees USING HASH (email);

-- GIN (Generalized Inverted Index — best for JSONB, arrays, full-text search)
CREATE INDEX idx_metadata ON orders USING GIN (metadata);

-- GiST (Generalized Search Tree — for geometry, range types, full-text)
CREATE INDEX idx_location ON stores USING GIST (geo_location);

-- BRIN (Block Range Index — for very large tables with naturally ordered data)
CREATE INDEX idx_created ON logs USING BRIN (created_at);

-- Partial Index (index only some rows)
CREATE INDEX idx_active ON employees(email) WHERE active = true;

-- Composite Index (multi-column)
CREATE INDEX idx_dept_sal ON employees(department, salary);
```

---

### Q13. What is `EXPLAIN` and `EXPLAIN ANALYZE`?

```sql
-- EXPLAIN: shows the PLAN without executing
EXPLAIN SELECT * FROM employees WHERE salary > 50000;

-- EXPLAIN ANALYZE: executes the query and shows actual timings
EXPLAIN ANALYZE SELECT * FROM employees WHERE salary > 50000;
```

**Things to look for:**
- `Seq Scan` → full table scan (slow on big tables — consider an index)
- `Index Scan` → using an index ✅
- `Nested Loop` vs `Hash Join` vs `Merge Join` → join strategies
- `Rows` (estimated vs actual) → big difference means outdated statistics → run `ANALYZE`

---

### Q14. What is the N+1 query problem? (Java/JPA specific)

```java
// BAD — N+1 problem: 1 query for orders + N queries for customers
List<Order> orders = orderRepo.findAll();          // 1 query
for (Order o : orders) {
    o.getCustomer().getName();                     // N queries (one per order)
}

// GOOD — Use JOIN FETCH in JPQL
@Query("SELECT o FROM Order o JOIN FETCH o.customer")
List<Order> findAllWithCustomer();                 // 1 query total
```

---

## Section 4 — Transactions & Concurrency

### Q15. What is a Transaction? What is ACID?

```sql
BEGIN;
    UPDATE accounts SET balance = balance - 500 WHERE id = 1;
    UPDATE accounts SET balance = balance + 500 WHERE id = 2;
COMMIT;   -- both succeed together
-- or
ROLLBACK; -- both are undone
```

| Letter | Stands For | Meaning |
|---|---|---|
| **A** | Atomicity | All-or-nothing — either all statements succeed or none |
| **C** | Consistency | DB goes from one valid state to another |
| **I** | Isolation | Concurrent transactions don't interfere |
| **D** | Durability | Once committed, data survives crashes |

---

### Q16. What are Transaction Isolation Levels?

```sql
SET TRANSACTION ISOLATION LEVEL REPEATABLE READ;
```

| Level | Dirty Read | Non-Repeatable Read | Phantom Read |
|---|---|---|---|
| `READ UNCOMMITTED` | ✅ Possible | ✅ Possible | ✅ Possible |
| `READ COMMITTED` (PG default) | ❌ Prevented | ✅ Possible | ✅ Possible |
| `REPEATABLE READ` | ❌ | ❌ | ✅ Possible (but PG prevents this too) |
| `SERIALIZABLE` | ❌ | ❌ | ❌ |

**Java/Spring equivalent:**
```java
@Transactional(isolation = Isolation.REPEATABLE_READ)
public void transferMoney() { ... }
```

---

### Q17. What is a Deadlock?

```
Transaction A                    Transaction B
─────────────                    ─────────────
LOCK row 1  ✅                   LOCK row 2  ✅
LOCK row 2  ⏳ waiting...        LOCK row 1  ⏳ waiting...
         ↑                              ↑
         └──── Both waiting for each other forever ────┘
```

**PostgreSQL detects deadlocks** and kills one transaction with:
```
ERROR: deadlock detected
```

**Prevention:** Always lock rows in a **consistent order** (e.g., by `id` ascending).

---

## Section 5 — Advanced (Differentiator Questions)

### Q18. What are CTEs (Common Table Expressions)?

```sql
-- Readable alternative to subqueries
WITH high_earners AS (
    SELECT name, salary, department FROM employees WHERE salary > 80000
)
SELECT department, COUNT(*) FROM high_earners GROUP BY department;

-- Recursive CTE (e.g., org hierarchy)
WITH RECURSIVE org_tree AS (
    SELECT id, name, manager_id, 1 AS level FROM employees WHERE manager_id IS NULL
    UNION ALL
    SELECT e.id, e.name, e.manager_id, ot.level + 1
    FROM employees e JOIN org_tree ot ON e.manager_id = ot.id
)
SELECT * FROM org_tree;
```

---

### Q19. What is `VACUUM` in PostgreSQL?

PostgreSQL uses **MVCC** — when you `UPDATE` or `DELETE`, the old row isn't physically removed. `VACUUM` reclaims that space.

| Command | What it does |
|---|---|
| `VACUUM` | Marks dead tuples as reusable (doesn't shrink file) |
| `VACUUM FULL` | Rewrites the entire table — reclaims disk space (locks table!) |
| `AUTOVACUUM` | Background process that runs VACUUM automatically ✅ |
| `ANALYZE` | Updates statistics so the query planner makes better choices |

---

### Q20. Stored Procedure vs Function in PostgreSQL?

```sql
-- FUNCTION: returns a value, can be used in SELECT
CREATE FUNCTION get_tax(price NUMERIC) RETURNS NUMERIC AS $$
BEGIN
    RETURN price * 0.18;
END;
$$ LANGUAGE plpgsql;

SELECT name, get_tax(price) FROM products;

-- PROCEDURE (PG 11+): does NOT return a value, can manage transactions
CREATE PROCEDURE transfer_funds(sender INT, receiver INT, amount NUMERIC)
LANGUAGE plpgsql AS $$
BEGIN
    UPDATE accounts SET balance = balance - amount WHERE id = sender;
    UPDATE accounts SET balance = balance + amount WHERE id = receiver;
    COMMIT;
END;
$$;

CALL transfer_funds(1, 2, 500);
```

---

### Q21. Views vs Materialized Views?

```sql
-- VIEW: virtual table — query runs every time you access it
CREATE VIEW active_employees AS
SELECT * FROM employees WHERE active = true;

-- MATERIALIZED VIEW: stores results physically — faster, but stale
CREATE MATERIALIZED VIEW monthly_sales AS
SELECT product_id, SUM(amount) FROM orders
WHERE order_date > CURRENT_DATE - INTERVAL '30 days'
GROUP BY product_id;

-- Must manually refresh
REFRESH MATERIALIZED VIEW monthly_sales;
REFRESH MATERIALIZED VIEW CONCURRENTLY monthly_sales; -- no lock
```

---

## Section 6 — Java + PostgreSQL Integration

### Q22. How do you connect PostgreSQL with a Spring Boot app?

```properties
# application.properties
spring.datasource.url=jdbc:postgresql://localhost:5432/mydb
spring.datasource.username=postgres
spring.datasource.password=secret
spring.datasource.driver-class-name=org.postgresql.Driver
spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect
spring.jpa.hibernate.ddl-auto=validate   # never use "create" in production!
```

```xml
<!-- pom.xml dependency -->
<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
    <scope>runtime</scope>
</dependency>
```

---

### Q23. What is Connection Pooling? Why use HikariCP?

Opening a DB connection is **expensive** (~5-10ms). A pool keeps connections open and reuses them.

```properties
# HikariCP (default in Spring Boot)
spring.datasource.hikari.maximum-pool-size=10
spring.datasource.hikari.minimum-idle=5
spring.datasource.hikari.connection-timeout=30000
spring.datasource.hikari.idle-timeout=600000
```

> Without pooling: 1000 requests = 1000 connections opened/closed.
> With pooling: 1000 requests = 10 connections reused.

---

### Q24. `@Transactional` in Spring — what happens under the hood?

```java
@Transactional
public void placeOrder(Order order) {
    orderRepo.save(order);           // INSERT
    inventoryService.reduce(order);  // UPDATE
    // if any exception → entire transaction rolls back
}
```

- Spring creates a **proxy** around your class.
- Before the method: `BEGIN` transaction.
- After success: `COMMIT`.
- On RuntimeException: `ROLLBACK`.

**Gotcha:** `@Transactional` doesn't work on `private` methods or **self-invocation** (calling a `@Transactional` method from within the same class).

---

## Quick-Fire Round (Commonly Asked)

| # | Question | One-Line Answer |
|---|---|---|
| 25 | `DELETE` vs `TRUNCATE` vs `DROP` | `DELETE` = row-by-row (logged, slow); `TRUNCATE` = remove all rows (fast, no WHERE); `DROP` = delete entire table |
| 26 | `SERIAL` vs `IDENTITY` | Both auto-increment; `GENERATED ALWAYS AS IDENTITY` is the SQL-standard way (PG 10+) |
| 27 | What is `COALESCE`? | Returns the first non-null value: `COALESCE(phone, email, 'N/A')` |
| 28 | What is `NULLIF`? | Returns NULL if two values are equal: `NULLIF(a, b)` |
| 29 | What is `CAST`? | Type conversion: `CAST('123' AS INTEGER)` or `'123'::INTEGER` |
| 30 | What is a Schema in PG? | A namespace inside a database. Default is `public`. |
| 31 | How to find duplicate rows? | `SELECT email, COUNT(*) FROM users GROUP BY email HAVING COUNT(*) > 1;` |
| 32 | `EXISTS` vs `IN` | `EXISTS` is faster for large subqueries (short-circuits); `IN` is fine for small lists |
| 33 | What is `ON CONFLICT` (UPSERT)? | `INSERT ... ON CONFLICT (email) DO UPDATE SET name = EXCLUDED.name;` |
| 34 | How to paginate? | `SELECT * FROM products ORDER BY id LIMIT 20 OFFSET 40;` (keyset pagination is better for large data) |
| 35 | What is `pg_stat_activity`? | System view showing currently running queries — useful for debugging slow queries |

---

This covers the **core PostgreSQL knowledge** expected of a Java developer with ~2 years of experience: schema design, queries, joins, indexes, transactions, performance tuning, and Spring Boot integration.


package br.com.fiap.postech.adapter.output.persistence;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.data.jpa.domain.Specification;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Executes a JPA {@link Specification} against mocked Criteria API objects so the
 * predicate-building logic can be covered by unit tests instead of Testcontainers.
 */
@SuppressWarnings({"unchecked", "rawtypes"})
public final class SpecificationProbe {

    private final Root<?> root;
    private final CriteriaQuery<?> query;
    private final CriteriaBuilder builder;
    private final CriteriaBuilder.SimpleCase<?, ?> simpleCase;
    private final Path<Object> path;
    private final Predicate predicate;
    private final Expression<Object> expression;
    private final Order order;
    private Predicate[] andPredicates;
    private int andCalls;
    private int orCalls;

    private SpecificationProbe() {
        this.root = mock(Root.class, org.mockito.Mockito.withSettings().lenient());
        this.query = mock(CriteriaQuery.class, org.mockito.Mockito.withSettings().lenient());
        this.simpleCase = mock(CriteriaBuilder.SimpleCase.class, org.mockito.Mockito.withSettings().lenient());
        this.path = mock(Path.class, org.mockito.Mockito.withSettings().lenient());
        this.predicate = mock(Predicate.class, org.mockito.Mockito.withSettings().lenient());
        this.expression = mock(Expression.class, org.mockito.Mockito.withSettings().lenient());
        this.order = mock(Order.class, org.mockito.Mockito.withSettings().lenient());
        this.builder = mock(CriteriaBuilder.class, this::answer);
        stubCriteriaApi();
    }

    private Object answer(org.mockito.invocation.InvocationOnMock invocation) throws Throwable {
        String method = invocation.getMethod().getName();
        Object[] arguments = invocation.getArguments();
        if ("selectCase".equals(method)) {
            return simpleCase;
        }
        if ("and".equals(method) || "or".equals(method)) {
            Predicate[] restrictions = asPredicates(arguments);
            if ("and".equals(method)) {
                andCalls++;
                andPredicates = restrictions;
            } else {
                orCalls++;
            }
            return predicate;
        }
        if ("asc".equals(method) || "desc".equals(method)) {
            return order;
        }
        if ("lower".equals(method) || "upper".equals(method) || "trim".equals(method)) {
            return expression;
        }
        if (isPredicateMethod(method)) {
            return predicate;
        }
        return org.mockito.Answers.RETURNS_DEFAULTS.answer(invocation);
    }

    private static boolean isPredicateMethod(String method) {
        return switch (method) {
            case "equal", "notEqual", "greaterThan", "greaterThanOrEqualTo", "lessThan", "lessThanOrEqualTo",
                 "like", "notLike", "not", "isNull", "isNotNull", "between", "in", "exists", "isEmpty", "isNotEmpty" -> true;
            default -> false;
        };
    }

    public static SpecificationProbe evaluate(Specification<?> specification) {
        SpecificationProbe probe = new SpecificationProbe();
        Object result = ((Specification) specification).toPredicate(probe.root, probe.query, probe.builder);
        if (result == null) {
            throw new AssertionError("Specification returned a null predicate");
        }
        return probe;
    }

    private void stubCriteriaApi() {
        when(root.get(anyString())).thenReturn(path);
        when(path.in(any(java.util.Collection.class))).thenReturn(predicate);
        when(simpleCase.when(any(), any())).thenAnswer(invocation -> simpleCase);
        when(simpleCase.otherwise(any())).thenAnswer(invocation -> expression);
    }

    private static Predicate[] asPredicates(Object[] arguments) {
        if (arguments.length == 1 && arguments[0] instanceof Predicate[] restrictions) {
            return restrictions;
        }
        return Arrays.stream(arguments)
                .filter(Predicate.class::isInstance)
                .map(Predicate.class::cast)
                .toArray(Predicate[]::new);
    }

    public Root<?> root() {
        return root;
    }

    public CriteriaQuery<?> query() {
        return query;
    }

    public CriteriaBuilder builder() {
        return builder;
    }

    public Predicate predicate() {
        return predicate;
    }

    public Optional<Path<Object>> path() {
        return Optional.of(path);
    }

    /**
     * Predicates combined by the last {@code criteriaBuilder.and(...)} call.
     */
    public List<Predicate> andPredicates() {
        return andPredicates == null ? List.of() : Arrays.asList(andPredicates);
    }

    public int andCalls() {
        return andCalls;
    }

    public int orCalls() {
        return orCalls;
    }

    public void verifyOrderWasApplied() {
        verify(query, atLeastOnce()).orderBy(any(Order[].class));
    }
}

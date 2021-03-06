package org.cryptocoinpartners.schema;

import java.util.List;

import javax.persistence.Basic;
import javax.persistence.Cacheable;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;

import org.cryptocoinpartners.enumeration.FeeMethod;
import org.cryptocoinpartners.util.PersistUtil;

/**
 * @author Tim Olson
 */
@Entity
@Cacheable
public class Exchange extends EntityBase {

    /**
     * 
     */

    public static Exchange forSymbolOrCreate(String symbol) {
        PersistUtil persistUtil = new PersistUtil();
        Exchange found = forSymbol(symbol);
        if (found == null) {
            found = new Exchange(symbol);
            persistUtil.insert(found);
        }
        return found;
    }

    public static Exchange forSymbolOrCreate(String symbol, int margin, double feeRate, FeeMethod feeMethod) {
        PersistUtil persistUtil = new PersistUtil();
        Exchange found = forSymbol(symbol);
        if (found == null) {
            found = new Exchange(symbol, margin, feeRate, feeMethod);
            persistUtil.insert(found);
        }
        return found;
    }

    public static Exchange forSymbolOrCreate(String symbol, int margin, double feeRate, FeeMethod feeMethod, double marginFeeRate, FeeMethod marginFeeMethod) {
        PersistUtil persistUtil = new PersistUtil();
        Exchange found = forSymbol(symbol);
        if (found == null) {
            found = new Exchange(symbol, margin, feeRate, feeMethod, marginFeeRate, marginFeeMethod);
            persistUtil.insert(found);
        }
        return found;
    }

    /** returns null if the symbol does not represent an existing exchange */
    public static Exchange forSymbol(String symbol) {
        PersistUtil persistUtil = new PersistUtil();
        return persistUtil.queryZeroOne(Exchange.class, "select e from Exchange e where symbol=?1", symbol);
    }

    public static List<String> allSymbols() {
        return PersistUtil.queryList(String.class, "select symbol from Exchange");
    }

    @Basic(optional = false)
    public String getSymbol() {
        return symbol;
    }

    @Basic(optional = false)
    public double getFeeRate() {
        return feeRate;
    }

    protected void setFeeRate(double feeRate) {
        this.feeRate = feeRate;
    }

    @Basic(optional = true)
    public double getMarginFeeRate() {
        return marginFeeRate;
    }

    protected void setMarginFeeRate(double marginFeeRate) {
        this.marginFeeRate = marginFeeRate;
    }

    @ManyToOne(optional = false)
    private FeeMethod feeMethod;

    public FeeMethod getFeeMethod() {
        return feeMethod;
    }

    protected void setFeeMethod(FeeMethod feeMethod) {
        this.feeMethod = feeMethod;
    }

    @ManyToOne(optional = true)
    private FeeMethod marginFeeMethod;

    public FeeMethod getMarginFeeMethod() {
        return marginFeeMethod;
    }

    protected void setMarginFeeMethod(FeeMethod marginFeeMethod) {
        this.marginFeeMethod = marginFeeMethod;
    }

    @Basic(optional = false)
    public int getMargin() {
        return Math.max(margin, 1);

    }

    protected void setMargin(int margin) {
        this.margin = margin;
    }

    @Override
    public String toString() {
        return symbol;
    }

    // JPA
    protected Exchange() {
    }

    protected void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    private Exchange(String symbol, int margin, double feeRate, FeeMethod feeMethod) {
        this.symbol = symbol;
        this.margin = margin;
        this.feeRate = feeRate;
        this.feeMethod = feeMethod;
    }

    private Exchange(String symbol, int margin, double feeRate, FeeMethod feeMethod, double marginFeeRate, FeeMethod marginFeeMethod) {
        this.symbol = symbol;
        this.margin = margin;
        this.feeRate = feeRate;
        this.feeMethod = feeMethod;
        this.marginFeeMethod = marginFeeMethod;
        this.marginFeeRate = marginFeeRate;
    }

    private Exchange(String symbol) {
        this.symbol = symbol;

    }

    private String symbol;
    private int margin;
    private double feeRate;
    private double marginFeeRate;

}

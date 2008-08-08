/*******************************************************************************
 * Copyright (c) 2008 SAP AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SAP AG - initial API and implementation
 *******************************************************************************/
package org.eclipse.mat.snapshot;

import java.net.URL;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.mat.SnapshotException;
import org.eclipse.mat.query.Column;
import org.eclipse.mat.query.IContextObject;
import org.eclipse.mat.query.IContextObjectSet;
import org.eclipse.mat.query.IIconProvider;
import org.eclipse.mat.query.IResultTable;
import org.eclipse.mat.query.IResultTree;
import org.eclipse.mat.query.ResultMetaData;
import org.eclipse.mat.snapshot.query.Icons;

/**
 * Class histogram - heap objects aggregated by their class. It holds the number
 * and consumed memory of the objects aggregated per class and aggregated per
 * class loader.
 */
public class Histogram extends HistogramRecord implements IResultTable, IIconProvider
{
    private static final long serialVersionUID = 2L;

    /* package */boolean isDefaultHistogram;
    private ArrayList<ClassHistogramRecord> classHistogramRecords;
    private ArrayList<ClassLoaderHistogramRecord> classLoaderHistogramRecords;

    /* package */Histogram()
    {
    // needed for serialization
    }

    public Histogram(String label, ArrayList<ClassHistogramRecord> classHistogramRecords,
                    ArrayList<ClassLoaderHistogramRecord> classLoaderHistogramRecords, long numberOfObjects,
                    long usedHeapSize, long retainedHeapSize)
    {
        this(label, classHistogramRecords, classLoaderHistogramRecords, numberOfObjects, usedHeapSize,
                        retainedHeapSize, false);
    }

    public Histogram(String label, ArrayList<ClassHistogramRecord> classHistogramRecords,
                    ArrayList<ClassLoaderHistogramRecord> classLoaderHistogramRecords, long numberOfObjects,
                    long usedHeapSize, long retainedHeapSize, boolean isDefaultHistogram)
    {
        super(label, numberOfObjects, usedHeapSize, retainedHeapSize);
        this.classHistogramRecords = classHistogramRecords;
        this.classLoaderHistogramRecords = classLoaderHistogramRecords;
        this.isDefaultHistogram = isDefaultHistogram;
    }

    /**
     * Get collection of all the classes for all the objects which were found in
     * the set of objects on which the class histogram was computed.
     * 
     * @return collection of all the classes for all the objects which were
     *         found in the set of objects on which the class histogram was
     *         computed
     */
    public Collection<ClassHistogramRecord> getClassHistogramRecords()
    {
        return classHistogramRecords;
    }

    /**
     * Get collection of all the class loaders for all the classes for all the
     * objects which were found in the set of objects on which the class
     * histogram was computed.
     * 
     * @return collection of all the class loaders for all the classes for all
     *         the objects which were found in the set of objects on which the
     *         class histogram was computed
     */
    public Collection<ClassLoaderHistogramRecord> getClassLoaderHistogramRecords()
    {
        return classLoaderHistogramRecords;
    }

    /**
     * Compute a new histogram as difference of this histogram compared to
     * (minus) the given baseline histogram.
     * <p>
     * This method can be used to check what has changed from one histogram to
     * another, to compute a delta.
     * 
     * @param baseline
     *            baseline histogram
     * @return difference histogram between this histogram compared to (minus)
     *         the given baseline histogram
     */
    public Histogram diffWithBaseline(Histogram baseline)
    {
        int classIdBase = -1000000000;
        int classLoaderIdBase = -2000000000;
        int classIdCurrent = classIdBase;
        int classLoaderIdCurrent = classLoaderIdBase;
        Map<String, Map<String, ClassHistogramRecord>> classLoaderDifferences = new HashMap<String, Map<String, ClassHistogramRecord>>();

        for (ClassLoaderHistogramRecord classLoaderHistogramRecord : classLoaderHistogramRecords)
        {
            String classLoaderName = classLoaderHistogramRecord.getLabel();
            Map<String, ClassHistogramRecord> classDifferences = classLoaderDifferences.get(classLoaderName);
            if (classDifferences == null)
            {
                classLoaderDifferences.put(classLoaderName,
                                classDifferences = new HashMap<String, ClassHistogramRecord>());
            }
            for (ClassHistogramRecord classHistogramRecord : classLoaderHistogramRecord.getClassHistogramRecords())
            {
                String className = classHistogramRecord.getLabel();
                ClassHistogramRecord classDifference = classDifferences.get(className);
                if (classDifference == null)
                {
                    classDifferences.put(className, classDifference = new ClassHistogramRecord(className,
                                    --classIdCurrent, 0, 0, 0));
                }
                classDifference.incNumberOfObjects(classHistogramRecord.getNumberOfObjects());
                classDifference.incUsedHeapSize(classHistogramRecord.getUsedHeapSize());
            }
        }
        for (ClassLoaderHistogramRecord classLoaderHistogramRecord : baseline.classLoaderHistogramRecords)
        {
            String classLoaderName = classLoaderHistogramRecord.getLabel();
            Map<String, ClassHistogramRecord> classDifferences = classLoaderDifferences.get(classLoaderName);
            if (classDifferences == null)
            {
                classLoaderDifferences.put(classLoaderName,
                                classDifferences = new HashMap<String, ClassHistogramRecord>());
            }
            for (ClassHistogramRecord classHistogramRecord : classLoaderHistogramRecord.getClassHistogramRecords())
            {
                String className = classHistogramRecord.getLabel();
                ClassHistogramRecord classDifference = classDifferences.get(className);
                if (classDifference == null)
                {
                    classDifferences.put(className, classDifference = new ClassHistogramRecord(className,
                                    --classIdCurrent, 0, 0, 0));
                }
                classDifference.incNumberOfObjects(-classHistogramRecord.getNumberOfObjects());
                classDifference.incUsedHeapSize(-classHistogramRecord.getUsedHeapSize());
            }
        }
        Map<String, ClassHistogramRecord> classDiffRecordsMerged = new HashMap<String, ClassHistogramRecord>();
        ArrayList<ClassLoaderHistogramRecord> classLoaderDiffRecordsMerged = new ArrayList<ClassLoaderHistogramRecord>();
        for (Map.Entry<String, Map<String, ClassHistogramRecord>> classDifferences : classLoaderDifferences.entrySet())
        {
            ArrayList<ClassHistogramRecord> records = new ArrayList<ClassHistogramRecord>(classDifferences.getValue()
                            .values().size());
            int numberOfObjects = 0;
            long usedHeapSize = 0;
            for (ClassHistogramRecord classDifference : classDifferences.getValue().values())
            {
                ClassHistogramRecord classDifferenceMerged = classDiffRecordsMerged.get(classDifference.getLabel());
                if (classDifferenceMerged == null)
                {
                    classDiffRecordsMerged.put(classDifference.getLabel(),
                                    classDifferenceMerged = new ClassHistogramRecord(classDifference.getLabel(),
                                                    --classIdCurrent, 0, 0, 0));
                }
                classDifferenceMerged.incNumberOfObjects(classDifference.getNumberOfObjects());
                classDifferenceMerged.incUsedHeapSize(classDifference.getUsedHeapSize());
                records.add(classDifference);
                numberOfObjects += classDifference.getNumberOfObjects();
                usedHeapSize += classDifference.getUsedHeapSize();
            }
            classLoaderDiffRecordsMerged.add(new ClassLoaderHistogramRecord(classDifferences.getKey(),
                            --classLoaderIdCurrent, records, numberOfObjects, usedHeapSize, 0));
        }
        return new Histogram("Histogram difference between " + getLabel() + " and " + baseline.getLabel(),
                        new ArrayList<ClassHistogramRecord>(classDiffRecordsMerged.values()),
                        classLoaderDiffRecordsMerged, //
                        Math.abs(this.getNumberOfObjects() - baseline.getNumberOfObjects()), //
                        Math.abs(this.getUsedHeapSize() - baseline.getUsedHeapSize()), //
                        Math.abs(this.getRetainedHeapSize() - baseline.getRetainedHeapSize()));
    }

    /**
     * Compute a new histogram as intersection of this histogram compared to
     * (equals) the given another histogram.
     * <p>
     * This method can be used to check what remains the same within two
     * histograms, e.g. if you have two histograms it shows what hasn't changed,
     * e.g. if you have two difference histograms it shows what remained the
     * same change (increase or decrease; used in gradient memory leak
     * analysis).
     * <p>
     * Note: Heap space is not taken into account in this analysis, only the
     * number of objects, i.e. when the number of objects is the same, you will
     * see this number of objects, otherwise or if there are no objects of a
     * particular class you won't get a histogram record for it!
     * 
     * @param another
     *            another histogram
     * @return intersection histogram of this histogram compared to (equals) the
     *         given another histogram
     */
    public Histogram intersectWithAnother(Histogram another)
    {
        int classIdBase = -1000000000;
        int classLoaderIdBase = -2000000000;
        int classIdCurrent = classIdBase;
        int classLoaderIdCurrent = classLoaderIdBase;
        Map<String, Map<String, ClassHistogramRecord>> classLoaderDifferences = new HashMap<String, Map<String, ClassHistogramRecord>>();
        for (ClassLoaderHistogramRecord classLoaderHistogramRecord : classLoaderHistogramRecords)
        {
            String classLoaderName = classLoaderHistogramRecord.getLabel();
            Map<String, ClassHistogramRecord> classDifferences = classLoaderDifferences.get(classLoaderName);
            if (classDifferences == null)
            {
                classLoaderDifferences.put(classLoaderName,
                                classDifferences = new HashMap<String, ClassHistogramRecord>());
            }
            for (ClassHistogramRecord classHistogramRecord : classLoaderHistogramRecord.getClassHistogramRecords())
            {
                String className = "Class$%" + classHistogramRecord.getLabel();
                ClassHistogramRecord classDifference = classDifferences.get(className);
                if (classDifference == null)
                {
                    classDifferences.put(className, classDifference = new ClassHistogramRecord(className,
                                    --classIdCurrent, 0, 0, 0));
                }
                classDifference.incNumberOfObjects(classHistogramRecord.getNumberOfObjects());
                classDifference.incUsedHeapSize(classHistogramRecord.getUsedHeapSize());
            }
        }
        for (ClassLoaderHistogramRecord classLoaderHistogramRecord : another.classLoaderHistogramRecords)
        {
            String classLoaderName = classLoaderHistogramRecord.getLabel();
            Map<String, ClassHistogramRecord> classDifferences = classLoaderDifferences.get(classLoaderName);
            if (classDifferences == null)
            {
                classLoaderDifferences.put(classLoaderName,
                                classDifferences = new HashMap<String, ClassHistogramRecord>());
            }
            for (ClassHistogramRecord classHistogramRecord : classLoaderHistogramRecord.getClassHistogramRecords())
            {
                String className = classHistogramRecord.getLabel();
                ClassHistogramRecord classDifference = classDifferences.get("Class$%" + className);
                if ((classDifference != null) && (classDifference.getNumberOfObjects() > 0)
                                && (classDifference.getNumberOfObjects() == classHistogramRecord.getNumberOfObjects()))
                {
                    classDifferences.put(className, new ClassHistogramRecord(className, --classIdCurrent,
                                    classDifference.getNumberOfObjects(), classDifference.getUsedHeapSize()
                                                    + classHistogramRecord.getUsedHeapSize(), 0));
                }
            }
        }
        Map<String, ClassHistogramRecord> classDiffRecordsMerged = new HashMap<String, ClassHistogramRecord>();
        ArrayList<ClassLoaderHistogramRecord> classLoaderDiffRecordsMerged = new ArrayList<ClassLoaderHistogramRecord>();
        int numberOfObjectsOverall = 0;
        long usedHeapSizeOverall = 0;
        for (Map.Entry<String, Map<String, ClassHistogramRecord>> classDifferences : classLoaderDifferences.entrySet())
        {
            ArrayList<ClassHistogramRecord> records = new ArrayList<ClassHistogramRecord>(classDifferences.getValue()
                            .values().size());
            int numberOfObjects = 0;
            long usedHeapSize = 0;
            for (ClassHistogramRecord classDifference : classDifferences.getValue().values())
            {
                if (!classDifference.getLabel().startsWith("Class$%"))
                {
                    ClassHistogramRecord classDifferenceMerged = classDiffRecordsMerged.get(classDifference.getLabel());
                    if (classDifferenceMerged == null)
                    {
                        classDiffRecordsMerged.put(classDifference.getLabel(),
                                        classDifferenceMerged = new ClassHistogramRecord(classDifference.getLabel(),
                                                        --classIdCurrent, 0, 0, 0));
                    }
                    classDifferenceMerged.incNumberOfObjects(classDifference.getNumberOfObjects());
                    classDifferenceMerged.incUsedHeapSize(classDifference.getUsedHeapSize());
                    records.add(classDifference);
                    numberOfObjects += classDifference.getNumberOfObjects();
                    usedHeapSize += classDifference.getUsedHeapSize();
                    numberOfObjectsOverall += numberOfObjects;
                    usedHeapSizeOverall += usedHeapSize;
                }
            }
            if (records.size() > 0)
            {
                classLoaderDiffRecordsMerged.add(new ClassLoaderHistogramRecord(classDifferences.getKey(),
                                --classLoaderIdCurrent, records, numberOfObjects, usedHeapSize, 0));
            }
        }
        return new Histogram("Histogram intersection of " + getLabel() + " and " + another.getLabel(),
                        new ArrayList<ClassHistogramRecord>(classDiffRecordsMerged.values()),
                        classLoaderDiffRecordsMerged, numberOfObjectsOverall, usedHeapSizeOverall, 0);
    }

    public boolean isDefaultHistogram()
    {
        return isDefaultHistogram;
    }

    @Override
    public String toString()
    {
        StringBuilder buf = new StringBuilder(1024);
        buf.append("Histogram ");
        buf.append(label);
        buf.append(" with ");
        buf.append((classLoaderHistogramRecords != null) ? classLoaderHistogramRecords.size() : 0);
        buf.append(" class loaders, ");
        buf.append((classHistogramRecords != null) ? classHistogramRecords.size() : 0);
        buf.append(" classes, ");
        buf.append(numberOfObjects);
        buf.append(" objects, ");
        buf.append(usedHeapSize);
        buf.append(" used heap bytes:");

        if (classHistogramRecords != null)
        {
            buf.append("\n\nCLASS STATISTICS:\n");
            buf.append(alignRight("Objects", 17));
            buf.append(alignRight("Shallow Heap", 17));
            buf.append(alignRight("Retained Heap", 17));
            buf.append("  ");
            buf.append(alignLeft("Class Name", 0));
            buf.append("\n");

            appendRecords(buf, classHistogramRecords);
        }

        if (classLoaderHistogramRecords != null)
        {
            buf.append("\n\nCLASSLOADER STATISTICS:\n");
            buf.append(alignRight("Objects", 17));
            buf.append(alignRight("Shallow Heap", 17));
            buf.append(alignRight("Retained Heap", 17));
            buf.append("  ");
            buf.append(alignLeft("Class Name", 0));
            buf.append("\n");

            appendRecords(buf, classLoaderHistogramRecords);
        }

        return buf.toString();
    }

    private static void appendRecords(StringBuilder summary, List<? extends HistogramRecord> records)
    {
        NumberFormat formatter = NumberFormat.getNumberInstance();
        for (HistogramRecord record : records)
        {
            summary.append(alignRight(formatter.format(record.getNumberOfObjects()), 17));
            summary.append(alignRight(formatter.format(record.getUsedHeapSize()), 17));
            if (record.getRetainedHeapSize() < 0)
                summary.append(alignRight(">=" + formatter.format(-record.getRetainedHeapSize()), 17));
            else
                summary.append(alignRight(formatter.format(record.getRetainedHeapSize()), 17));
            summary.append("  ");
            summary.append(alignLeft(record.getLabel(), 0));
            summary.append("\n");
        }
    }

    private static String alignLeft(String text, int length)
    {
        if (text.length() >= length) { return text; }
        StringBuilder buf = new StringBuilder(length);
        int blanks = length - text.length();
        buf.append(text);
        for (int i = 0; i < blanks; i++)
        {
            buf.append(' ');
        }
        return buf.toString();
    }

    private static String alignRight(String text, int length)
    {
        if (text.length() >= length) { return text; }
        StringBuilder buf = new StringBuilder(length);
        int blanks = length - text.length();
        for (int i = 0; i < blanks; i++)
        {
            buf.append(' ');
        }
        buf.append(text);
        return buf.toString();
    }

    /**
     * Generate human readable text based report from a histogram.
     * 
     * @param histogram
     *            histogram you want a human reable text based report for
     * @param comparator
     *            comparator to be used for sorting the histogram records (
     *            {@link HistogramRecord} provides some default comparators)
     * @return human redable text based report for the given histogram
     */
    public static String generateClassHistogramRecordTextReport(Histogram histogram,
                    Comparator<HistogramRecord> comparator)
    {
        return generateHistogramRecordTextReport(new ArrayList<HistogramRecord>(histogram.getClassHistogramRecords()),
                        comparator, new String[] { "Class Name", "Objects", "Heap", "Retained Heap" });
    }

    private static String generateHistogramRecordTextReport(List<HistogramRecord> records,
                    Comparator<HistogramRecord> comparator, String[] headers)
    {
        Collections.sort(records, comparator);
        int labelLength = headers[0].length();
        int numberOfObjectsLength = headers[1].length();
        int usedHeapSizeLength = headers[2].length();
        int retainedHeapSizeLength = headers[3].length();
        for (HistogramRecord record : records)
        {
            if (record.getLabel().length() > labelLength)
            {
                labelLength = record.getLabel().length();
            }
            if (Long.toString(record.getNumberOfObjects()).length() > numberOfObjectsLength)
            {
                numberOfObjectsLength = Long.toString(record.getNumberOfObjects()).length();
            }
            if (Long.toString(record.getUsedHeapSize()).length() > usedHeapSizeLength)
            {
                usedHeapSizeLength = Long.toString(record.getUsedHeapSize()).length();
            }
            if (Long.toString(record.getRetainedHeapSize()).length() > retainedHeapSizeLength)
            {
                retainedHeapSizeLength = Long.toString(record.getRetainedHeapSize()).length();
            }
        }
        StringBuilder report = new StringBuilder((4 + records.size())
                        * (2 + labelLength + 3 + numberOfObjectsLength + 3 + usedHeapSizeLength + 3
                                        + retainedHeapSizeLength + 2 + 2));
        appendStringAndFillUp(report, null, '-', 2 + labelLength + 3 + numberOfObjectsLength + 3 + usedHeapSizeLength
                        + 3 + retainedHeapSizeLength + 2);
        report.append("\r\n");
        report.append("| ");
        appendStringAndFillUp(report, headers[0], ' ', labelLength);
        report.append(" | ");
        appendStringAndFillUp(report, headers[1], ' ', numberOfObjectsLength);
        report.append(" | ");
        appendStringAndFillUp(report, headers[2], ' ', usedHeapSizeLength);
        report.append(" | ");
        appendStringAndFillUp(report, headers[3], ' ', retainedHeapSizeLength);
        report.append(" |\r\n");
        appendStringAndFillUp(report, null, '-', 2 + labelLength + 3 + numberOfObjectsLength + 3 + usedHeapSizeLength
                        + 3 + retainedHeapSizeLength + 2);
        report.append("\r\n");
        for (HistogramRecord record : records)
        {
            report.append("| ");
            appendStringAndFillUp(report, record.getLabel(), ' ', labelLength);
            report.append(" | ");
            appendPreFillAndString(report, Long.toString(record.getNumberOfObjects()), ' ', numberOfObjectsLength);
            report.append(" | ");
            appendPreFillAndString(report, Long.toString(record.getUsedHeapSize()), ' ', usedHeapSizeLength);
            report.append(" | ");
            appendPreFillAndString(report, Long.toString(record.getRetainedHeapSize()), ' ', retainedHeapSizeLength);
            report.append(" |\r\n");
        }
        appendStringAndFillUp(report, null, '-', 2 + labelLength + 3 + numberOfObjectsLength + 3 + usedHeapSizeLength
                        + 3 + retainedHeapSizeLength + 2);
        report.append("\r\n");
        return report.toString();
    }

    /**
     * Generate machine/human readable comma separated report from an histogram.
     * 
     * @param histogram
     *            histogram you want a machine/human readable comma separated
     *            report for
     * @param comparator
     *            comparator to be used for sorting the histogram records (
     *            {@link HistogramRecord} provides some default comparators)
     * @return machine/human readable comma separated report for the given
     *         histogram
     */
    public static String generateClassHistogramRecordCsvReport(Histogram histogram,
                    Comparator<HistogramRecord> comparator)
    {
        return generateHistogramRecordCsvReport(new ArrayList<ClassHistogramRecord>(histogram
                        .getClassHistogramRecords()), comparator, new String[] { "Class Name", "Objects",
                        "Shallow Heap", "Retained Heap" });
    }

    /**
     * Generate machine/human readable comma separated report from an histogram.
     * 
     * @param histogram
     *            histogram you want a machine/human readable comma separated
     *            report for
     * @param comparator
     *            comparator to be used for sorting the histogram records (
     *            {@link HistogramRecord} provides some default comparators)
     * @return machine/human readable comma separated report for the given
     *         histogram
     */
    public static String generateClassLoaderHistogramRecordCsvReport(Histogram histogram,
                    Comparator<HistogramRecord> comparator)
    {
        return generateClassloaderHistogramCsvReport(new ArrayList<ClassLoaderHistogramRecord>(histogram
                        .getClassLoaderHistogramRecords()), comparator, new String[] { "ClassLoader Name",
                        "Class Name", "Objects", "Shallow Heap", "Retained Heap" });
    }

    private static String generateClassloaderHistogramCsvReport(List<ClassLoaderHistogramRecord> records,
                    Comparator<HistogramRecord> comparator, String[] headers)
    {
        StringBuilder report = new StringBuilder((1 + records.size()) * 256);
        report.append(headers[0]);
        report.append(";");
        report.append(headers[1]);
        report.append(";");
        report.append(headers[2]);
        report.append(";");
        report.append(headers[3]);
        report.append(";");
        report.append(headers[4]);
        report.append(";\r\n");
        for (ClassLoaderHistogramRecord classloaderRecord : records)
        {
            Collection<ClassHistogramRecord> classRecords = ((ClassLoaderHistogramRecord) classloaderRecord)
                            .getClassHistogramRecords();
            List<ClassHistogramRecord> list = new ArrayList<ClassHistogramRecord>(classRecords);
            Collections.sort(list, Histogram.COMPARATOR_FOR_USEDHEAPSIZE);
            for (int i = list.size() - 1; i >= 0; i--)
            {
                ClassHistogramRecord record = list.get(i);
                report.append(classloaderRecord.getLabel());
                report.append(";");
                report.append(record.getLabel());
                report.append(";");
                report.append(record.getNumberOfObjects());
                report.append(";");
                report.append(record.getUsedHeapSize());
                report.append(";");
                if (record.getRetainedHeapSize() < 0)
                    report.append(">=" + (-record.getRetainedHeapSize()));
                else
                    report.append(record.getRetainedHeapSize());
                report.append(";\r\n");
            }
        }
        return report.toString();
    }

    private static String generateHistogramRecordCsvReport(List<ClassHistogramRecord> records,
                    Comparator<HistogramRecord> comparator, String[] headers)
    {
        Collections.sort(records, comparator);
        StringBuilder report = new StringBuilder((1 + records.size()) * 256);
        report.append(headers[0]);
        report.append(";");
        report.append(headers[1]);
        report.append(";");
        report.append(headers[2]);
        report.append(";");
        report.append(headers[3]);
        report.append(";\r\n");
        for (HistogramRecord record : records)
        {
            report.append(record.getLabel());
            report.append(";");
            report.append(record.getNumberOfObjects());
            report.append(";");
            report.append(record.getUsedHeapSize());
            report.append(";");
            if (record.getRetainedHeapSize() < 0)
                report.append(">=" + (-record.getRetainedHeapSize()));
            else
                report.append(record.getRetainedHeapSize());
            report.append(";\r\n");
        }
        return report.toString();
    }

    private static void appendStringAndFillUp(StringBuilder report, String string, char character, int completeLength)
    {
        if (string != null)
        {
            report.append(string);
        }
        if (string != null)
        {
            completeLength -= string.length();
        }
        if (completeLength > 0)
        {
            for (int i = 0; i < completeLength; i++)
            {
                report.append(character);
            }
        }
    }

    private static void appendPreFillAndString(StringBuilder report, String string, char character, int completeLength)
    {
        if (string != null)
        {
            completeLength -= string.length();
        }
        if (completeLength > 0)
        {
            for (int i = 0; i < completeLength; i++)
            {
                report.append(character);
            }
        }
        if (string != null)
        {
            report.append(string);
        }
    }

    // //////////////////////////////////////////////////////////////
    // implementation as a IResultTable
    // //////////////////////////////////////////////////////////////

    public ResultMetaData getResultMetaData()
    {
        return null;
    }

    public Column[] getColumns()
    {
        return new Column[] { new Column("Class Name", String.class).comparing(HistogramRecord.COMPARATOR_FOR_LABEL), //
                        new Column("Objects", long.class).comparing(HistogramRecord.COMPARATOR_FOR_NUMBEROFOBJECTS), //
                        new Column("Shallow Heap", long.class) //
                                        .sorting(Column.SortDirection.DESC) //
                                        .comparing(HistogramRecord.COMPARATOR_FOR_USEDHEAPSIZE) };
    }

    public int getRowCount()
    {
        return classHistogramRecords.size();
    }

    public Object getRow(int rowId)
    {
        return classHistogramRecords.get(rowId);
    }

    public Object getColumnValue(Object row, int columnIndex)
    {
        ClassHistogramRecord record = (ClassHistogramRecord) row;
        switch (columnIndex)
        {
            case 0:
                return record.getLabel();
            case 1:
                return record.getNumberOfObjects();
            case 2:
                return record.getUsedHeapSize();
        }
        return null;
    }

    public IContextObject getContext(final Object row)
    {
        final ClassHistogramRecord record = (ClassHistogramRecord) row;

        if (record.getClassId() < 0)
            return null;

        return new IContextObjectSet()
        {
            public int getObjectId()
            {
                return record.getClassId();
            }

            public int[] getObjectIds()
            {
                return record.getObjectIds();
            }

            public String getOQL()
            {
                return isDefaultHistogram ? OQL.forObjectsOfClass(record.getClassId()) : null;
            }
        };
    }

    public URL getIcon(Object row)
    {
        return Icons.CLASS;
    }

    // //////////////////////////////////////////////////////////////
    // implementation as result tree grouped by class loader
    // //////////////////////////////////////////////////////////////

    public IResultTree groupByClassLoader()
    {
        return new ClassLoaderTree(this);
    }

    public static class ClassLoaderTree implements IResultTree, IIconProvider
    {
        Histogram histogram;

        public ClassLoaderTree(Histogram histogram)
        {
            this.histogram = histogram;
        }

        public Histogram getHistogram()
        {
            return histogram;
        }

        public ResultMetaData getResultMetaData()
        {
            return null;
        }

        public Column[] getColumns()
        {
            return new Column[] {
                            new Column("Class Loader / Class", String.class)
                                            .comparing(HistogramRecord.COMPARATOR_FOR_LABEL), //
                            new Column("Objects", long.class) //
                                            .comparing(HistogramRecord.COMPARATOR_FOR_NUMBEROFOBJECTS), //
                            new Column("Shallow Heap", long.class) //
                                            .sorting(Column.SortDirection.DESC)//
                                            .comparing(HistogramRecord.COMPARATOR_FOR_USEDHEAPSIZE) };
        }

        public List<?> getElements()
        {
            return histogram.classLoaderHistogramRecords;
        }

        public boolean hasChildren(Object element)
        {
            return element instanceof ClassLoaderHistogramRecord;
        }

        public List<?> getChildren(Object parent)
        {
            return new ArrayList<ClassHistogramRecord>(((ClassLoaderHistogramRecord) parent).getClassHistogramRecords());
        }

        public Object getColumnValue(Object row, int columnIndex)
        {
            HistogramRecord record = (HistogramRecord) row;
            switch (columnIndex)
            {
                case 0:
                    return record.getLabel();
                case 1:
                    return record.getNumberOfObjects();
                case 2:
                    return record.getUsedHeapSize();
            }
            return null;
        }

        public IContextObject getContext(Object row)
        {
            if (row instanceof ClassLoaderHistogramRecord)
            {
                final ClassLoaderHistogramRecord record = (ClassLoaderHistogramRecord) row;

                if (record.getClassLoaderId() < 0)
                    return null;

                return new IContextObjectSet()
                {
                    public int getObjectId()
                    {
                        return record.getClassLoaderId();
                    }

                    public int[] getObjectIds()
                    {
                        try
                        {
                            return record.getObjectIds();
                        }
                        catch (SnapshotException e)
                        {
                            throw new RuntimeException(e);
                        }
                    }

                    public String getOQL()
                    {
                        if (histogram.isDefaultHistogram)
                            return OQL.classesByClassLoaderId(record.getClassLoaderId());
                        else
                            return null;
                    }
                };
            }
            else if (row instanceof ClassHistogramRecord)
            {
                final ClassHistogramRecord record = (ClassHistogramRecord) row;
                if (record.getClassId() < 0)
                    return null;

                return new IContextObjectSet()
                {
                    public int getObjectId()
                    {
                        return record.getClassId();
                    }

                    public int[] getObjectIds()
                    {
                        return record.getObjectIds();
                    }

                    public String getOQL()
                    {
                        if (histogram.isDefaultHistogram)
                            return OQL.forObjectsOfClass(record.getClassId());
                        else
                            return null;
                    }
                };
            }
            else
            {
                return null;
            }

        }

        public URL getIcon(Object row)
        {
            return row instanceof ClassLoaderHistogramRecord ? Icons.CLASSLOADER_INSTANCE : Icons.CLASS;
        }
    }
}

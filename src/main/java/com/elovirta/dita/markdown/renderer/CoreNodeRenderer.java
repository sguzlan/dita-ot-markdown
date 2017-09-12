/*
 * Based on ToHtmlSerializer (C) 2010-2011 Mathias Doenitz
 */
package com.elovirta.dita.markdown.renderer;

import com.elovirta.dita.markdown.*;
import com.vladsch.flexmark.ast.*;
import com.vladsch.flexmark.ext.abbreviation.Abbreviation;
import com.vladsch.flexmark.ext.anchorlink.AnchorLink;
import com.vladsch.flexmark.ext.definition.DefinitionItem;
import com.vladsch.flexmark.ext.definition.DefinitionList;
import com.vladsch.flexmark.ext.definition.DefinitionTerm;
import com.vladsch.flexmark.ext.gfm.strikethrough.Strikethrough;
import com.vladsch.flexmark.ext.gfm.tables.TableBlock;
import com.vladsch.flexmark.ext.gfm.tables.TableBody;
import com.vladsch.flexmark.ext.gfm.tables.TableHead;
import com.vladsch.flexmark.ext.gfm.tables.TableRow;
import com.vladsch.flexmark.ext.typographic.TypographicQuotes;
import com.vladsch.flexmark.parser.ListOptions;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.options.DataHolder;
import org.apache.commons.io.FilenameUtils;
import org.dita.dost.util.DitaClass;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import java.net.URI;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.StreamSupport;

import static com.elovirta.dita.markdown.MetadataSerializerImpl.buildAtts;
import static javax.xml.XMLConstants.NULL_NS_URI;
import static javax.xml.XMLConstants.XML_NS_URI;
import static org.dita.dost.util.Constants.*;
import static org.dita.dost.util.URLUtils.toURI;
import static org.dita.dost.util.XMLUtils.AttributesBuilder;

public class CoreNodeRenderer extends SaxSerializer implements NodeRenderer {

    private static final String COLUMN_NAME_COL = "col";

    private static final Attributes TOPIC_ATTS = new AttributesBuilder().add(ATTRIBUTE_NAME_CLASS, TOPIC_TOPIC.toString())
            .add(DITA_NAMESPACE, ATTRIBUTE_NAME_DITAARCHVERSION, ATTRIBUTE_PREFIX_DITAARCHVERSION + ":" + ATTRIBUTE_NAME_DITAARCHVERSION, "CDATA", "1.2")
            .add(ATTRIBUTE_NAME_DOMAINS, "(topic hi-d) (topic ut-d) (topic indexing-d) (topic hazard-d) (topic abbrev-d) (topic pr-d) (topic sw-d) (topic ui-d)")
            .build();
    private static final Attributes BODY_ATTS = buildAtts(TOPIC_BODY);
    private static final Attributes SECTION_ATTS = buildAtts(TOPIC_SECTION);
    private static final Attributes EXAMPLE_ATTS = buildAtts(TOPIC_EXAMPLE);
    private static final Attributes LI_ATTS = buildAtts(TOPIC_LI);
    private static final Attributes P_ATTS = buildAtts(TOPIC_P);
    private static final Attributes I_ATTS = buildAtts(HI_D_I);
    private static final Attributes B_ATTS = buildAtts(HI_D_B);
    private static final Attributes DD_ATTS = buildAtts(TOPIC_DD);
    private static final Attributes CODEPH_ATTS = buildAtts(PR_D_CODEPH);
    private static final Attributes CODEBLOCK_ATTS = buildAtts(PR_D_CODEBLOCK);
    private static final Attributes DT_ATTS = buildAtts(TOPIC_DT);
    private static final Attributes DEL_ATTS = new AttributesBuilder().add(ATTRIBUTE_NAME_CLASS, TOPIC_PH.toString()).add("status", "deleted").build();
    private static final Attributes TITLE_ATTS = buildAtts(TOPIC_TITLE);
    private static final Attributes PROLOG_ATTS = buildAtts(TOPIC_PROLOG);
    private static final Attributes BLOCKQUOTE_ATTS = buildAtts(TOPIC_LQ);
    private static final Attributes UL_ATTS = buildAtts(TOPIC_UL);
    private static final Attributes DL_ATTS = buildAtts(TOPIC_DL);
    private static final Attributes DLENTRY_ATTS = buildAtts(TOPIC_DLENTRY);
    private static final Attributes OL_ATTS = buildAtts(TOPIC_OL);
    private static final Attributes TABLE_ATTS = buildAtts(TOPIC_TABLE);
    private static final Attributes TGROUP_ATTS = buildAtts(TOPIC_TGROUP);
    private static final Attributes COLSPEC_ATTS = buildAtts(TOPIC_COLSPEC);
    private static final Attributes TBODY_ATTS = buildAtts(TOPIC_TBODY);
    private static final Attributes THEAD_ATTS = buildAtts(TOPIC_THEAD);
    private static final Attributes TR_ATTS = buildAtts(TOPIC_ROW);
    private static final Attributes IMAGE_ATTS = buildAtts(TOPIC_IMAGE);
    private static final Attributes XREF_ATTS = buildAtts(TOPIC_XREF);
    private static final Attributes ALT_ATTS = buildAtts(TOPIC_ALT);
    private static final Attributes PH_ATTS = buildAtts(TOPIC_PH);
    private static final Attributes ENTRY_ATTS = buildAtts(TOPIC_ENTRY);
    private static final Attributes FIG_ATTS = buildAtts(TOPIC_FIG);
    private static final Attributes EMPTY_ATTS = new AttributesImpl();

    private static final Map<String, DitaClass> sections = new HashMap<>();

    static {
        sections.put(TOPIC_SECTION.localName, TOPIC_SECTION);
        sections.put(TOPIC_EXAMPLE.localName, TOPIC_EXAMPLE);
    }

//    private final Map<String, Object> documentMetadata;
    private final Map<String, ReferenceNode> references = new HashMap<>();
    private final Map<String, String> abbreviations = new HashMap<>();
//    private final MetadataSerializer metadataSerializer;
    private TableBlock currentTableNode;
    private int currentTableColumn;

    private boolean inSection = false;

    /**
     * Current header level.
     */
    private int headerLevel = 0;

    public CoreNodeRenderer(DataHolder options) {
//        this.referenceRepository = options.get(Parser.REFERENCES);
//        this.listOptions = ListOptions.getFrom(options);
//        this.recheckUndefinedReferences = HtmlRenderer.RECHECK_UNDEFINED_REFERENCES.getFrom(options);
//        this.obfuscateEmail = HtmlRenderer.OBFUSCATE_EMAIL.getFrom(options);
//        this.obfuscateEmailRandom = HtmlRenderer.OBFUSCATE_EMAIL_RANDOM.getFrom(options);
//        this.codeContentBlock = Parser.FENCED_CODE_CONTENT_BLOCK.getFrom(options);
//        codeSoftLineBreaks = Parser.CODE_SOFT_LINE_BREAKS.getFrom(options);
//        myLines = null;
//        myEOLs = null;
//        myNextLine = 0;
//        nextLineStartOffset = 0;
    }

    CoreNodeRenderer(final ContentHandler contentHandler, final Map<String, Object> documentMetadata) {
//        this.documentMetadata = documentMetadata;
        setContentHandler(contentHandler);
//        metadataSerializer = new MetadataSerializerImpl();
//        metadataSerializer.setContentHandler(contentHandler);

    }

    @Override
    public Set<NodeRenderingHandler<?>> getNodeRenderingHandlers() {
        return new HashSet<>(Arrays.asList(
                new NodeRenderingHandler<AutoLink>(AutoLink.class, (node, context, html) -> render(node, context, html)),
                new NodeRenderingHandler<BlockQuote>(BlockQuote.class, (node, context, html) -> render(node, context, html)),
                new NodeRenderingHandler<BulletList>(BulletList.class, (node, context, html) -> render(node, context, html)),
                new NodeRenderingHandler<Code>(Code.class, (node, context, html) -> render(node, context, html)),
                new NodeRenderingHandler<CodeBlock>(CodeBlock.class, (node, context, html) -> render(node, context, html)),
                new NodeRenderingHandler<Document>(Document.class, (node, context, html) -> render(node, context, html)),
                new NodeRenderingHandler<Emphasis>(Emphasis.class, (node, context, html) -> render(node, context, html)),
                new NodeRenderingHandler<FencedCodeBlock>(FencedCodeBlock.class, (node, context, html) -> render(node, context, html)),
                new NodeRenderingHandler<HardLineBreak>(HardLineBreak.class, (node, context, html) -> render(node, context, html)),
                new NodeRenderingHandler<Heading>(Heading.class, (node, context, html) -> render(node, context, html)),
                new NodeRenderingHandler<HtmlBlock>(HtmlBlock.class, (node, context, html) -> render(node, context, html)),
                new NodeRenderingHandler<HtmlCommentBlock>(HtmlCommentBlock.class, (node, context, html) -> render(node, context, html)),
                new NodeRenderingHandler<HtmlInnerBlock>(HtmlInnerBlock.class, (node, context, html) -> render(node, context, html)),
                new NodeRenderingHandler<HtmlInnerBlockComment>(HtmlInnerBlockComment.class, (node, context, html) -> render(node, context, html)),
                new NodeRenderingHandler<HtmlEntity>(HtmlEntity.class, (node, context, html) -> render(node, context, html)),
                new NodeRenderingHandler<HtmlInline>(HtmlInline.class, (node, context, html) -> render(node, context, html)),
                new NodeRenderingHandler<HtmlInlineComment>(HtmlInlineComment.class, (node, context, html) -> render(node, context, html)),
                new NodeRenderingHandler<Image>(Image.class, (node, context, html) -> render(node, context, html)),
                new NodeRenderingHandler<ImageRef>(ImageRef.class, (node, context, html) -> render(node, context, html)),
                new NodeRenderingHandler<IndentedCodeBlock>(IndentedCodeBlock.class, (node, context, html) -> render(node, context, html)),
                new NodeRenderingHandler<Link>(Link.class, (node, context, html) -> render(node, context, html)),
                new NodeRenderingHandler<LinkRef>(LinkRef.class, (node, context, html) -> render(node, context, html)),
                new NodeRenderingHandler<BulletListItem>(BulletListItem.class, (node, context, html) -> render(node, context, html)),
                new NodeRenderingHandler<OrderedListItem>(OrderedListItem.class, (node, context, html) -> render(node, context, html)),
                new NodeRenderingHandler<MailLink>(MailLink.class, (node, context, html) -> render(node, context, html)),
                new NodeRenderingHandler<OrderedList>(OrderedList.class, (node, context, html) -> render(node, context, html)),
                new NodeRenderingHandler<Paragraph>(Paragraph.class, (node, context, html) -> render(node, context, html)),
                new NodeRenderingHandler<Reference>(Reference.class, (node, context, html) -> render(node, context, html)),
                new NodeRenderingHandler<SoftLineBreak>(SoftLineBreak.class, (node, context, html) -> render(node, context, html)),
                new NodeRenderingHandler<StrongEmphasis>(StrongEmphasis.class, (node, context, html) -> render(node, context, html)),
                new NodeRenderingHandler<Text>(Text.class, (node, context, html) -> render(node, context, html)),
                new NodeRenderingHandler<TextBase>(TextBase.class, (node, context, html) -> render(node, context, html)),
                new NodeRenderingHandler<ThematicBreak>(ThematicBreak.class, (node, context, html) -> render(node, context, html)),
                new NodeRenderingHandler<AnchorLink>(AnchorLink.class, (node, context, html) -> render(node, context, html))
        ));
    }

//    void toHtml(final Document astRoot) throws SAXException {
////        checkArgNotNull(astRoot, "astRoot");
//        clean(astRoot);
//        final boolean isCompound = hasMultipleTopLevelHeaders(astRoot);
//        contentHandler.startDocument();
//        contentHandler.startPrefixMapping(ATTRIBUTE_PREFIX_DITAARCHVERSION, DITA_NAMESPACE);
//        if (isCompound) {
//            contentHandler.startElement(NULL_NS_URI, ELEMENT_NAME_DITA, ELEMENT_NAME_DITA, EMPTY_ATTS);
//        }
//        try {
//            astRoot.accept(this);
//            while (!tagStack.isEmpty()) {
//                html.endElement();
//            }
//        } catch (final ParseException e) {
//            //e.printStackTrace();
//            throw new SAXException("Failed to parse Markdown: " + e.getMessage(), e);
//        }
//        if (isCompound) {
//            contentHandler.endElement(NULL_NS_URI, ELEMENT_NAME_DITA, ELEMENT_NAME_DITA);
//        }
//        contentHandler.endPrefixMapping(ATTRIBUTE_PREFIX_DITAARCHVERSION);
//        contentHandler.endDocument();
//    }

    private boolean hasMultipleTopLevelHeaders(Document astRoot) {
        final long count = StreamSupport.stream(astRoot.getChildren().spliterator(), false)
                .filter(n -> (n instanceof Heading) && (((Heading) n).getLevel() == 1))
                .count();
        return count > 1;
    }

    /**
     * Replace metadata para with actual metadata element. Modifies AST <b>in-place</b>.
     */
    private void clean(final Document node) {
//        final Map<String, String> metadata = new HashMap<>();
//
//        // read pandoc_title_block
//        final Node first = node.getChildren().iterator().next();
//        if (first instanceof Paragraph && toString(first).startsWith("%")) {
//            final String[] fields = toString(first).split("\n");
//            if (fields.length >= 1) {
//                metadata.put("title", fields[0].substring(1));
//            }
////            if (fields.length >= 2) {
////                metadata.put("authors", fields[0].substring(1));
////            }
////            if (fields.length >= 3) {
////                metadata.put("date", fields[0].substring(1));
////            }
//        }
//
//        // insert header
//        if (metadata.containsKey("title")) {
//            boolean levelOneFound = false;
//            for (final Node c : node.getChildren()) {
//                if (c instanceof Heading) {
//                    final Heading h = (Heading) c;
//                    if (h.getLevel() == 1) {
//                        levelOneFound = true;
//                        break;
//                    }
//                }
//            }
//            final Node m;
//            if (levelOneFound) {
//                m = new MetadataNode(metadata.get("title"));
//            } else {
////                m = new Heading(1, new Text(metadata.get("title")));
//                m = new Heading(BasedSequenceImpl.of(metadata.get("title")));
//            }
//            m.insertBefore(node.getFirstChild());
////            node.getChildren().set(0, m);
//        }
    }

    // Visitor methods
//
//    private static void toString(final Node node, final StringBuilder buf) {
//        if (node instanceof SuperNode) {
//            for (final Node n : node.getChildren()) {
//                toString(n, buf);
//            }
//        } else if (node instanceof Text) {
//            buf.append(((Text) node).getChars());
//            for (final Node n : node.getChildren()) {
//                toString(n, buf);
//            }
//        } else if (node instanceof SimpleNode) {
//            buf.append(toString((SimpleNode) node));
//        } else {
//            throw new ParseException("Trying to process " + node);
//        }
//    }

//    private static String toString(final Node node) {
//        final StringBuilder buf = new StringBuilder();
//        toString(node, buf);
//        return buf.toString();
//    }

    private void render(final Document node, final NodeRendererContext context, final DitaWriter html) {
//        for (final ReferenceNode refNode : node.getReferences()) {
//            references.put(normalize(toString(refNode)), refNode);
//        }
//        for (final AbbreviationNode abbrNode : node.getAbbreviations()) {
//            visitChildren(abbrNode);
//            String abbr = printer.getString();
//            printer.clear();
//            abbrNode.getExpansion().accept(this);
//            String expansion = printer.getString();
//            abbreviations.put(abbr, expansion);
//            printer.clear();
//        }
        context.renderChildren(node);
//        context.renderChildren(node);
    }

    private void render(final Abbreviation node, final NodeRendererContext context, final DitaWriter html) {
    }

    private void render(AnchorLink node, final NodeRendererContext context, final DitaWriter html) {
        context.renderChildren(node);
    }
//
//    private void render(final AutoLinkNode node, final NodeRendererContext context, final DitaWriter html) {
//        final AttributesBuilder atts = getLinkAttributes(node.getChars().toString());
//
//        html.startElement(TOPIC_XREF, atts.build());
//        context.renderChildren(node);
//        html.endElement();
//    }

    private void render(final BlockQuote node, final NodeRendererContext context, final DitaWriter html) {
        printTag(node, context, html, TOPIC_LQ, BLOCKQUOTE_ATTS);
    }

    private void render(final BulletList node, final NodeRendererContext context, final DitaWriter html) {
        printTag(node, context, html, TOPIC_UL, UL_ATTS);
    }

    private void render(final Code node, final NodeRendererContext context, final DitaWriter html) {
        printTag(node, context, html, PR_D_CODEPH, CODEPH_ATTS);
    }

    private void render(final DefinitionList node, final NodeRendererContext context, final DitaWriter html) {
        html.startElement(TOPIC_DL, DL_ATTS);
        DitaClass previous = null;
//        for (final Node child : node.getChildren()) {
//            if (previous == null) {
//                html.startElement(TOPIC_DLENTRY, DLENTRY_ATTS);
//            }
//            if (child instanceof DefinitionTermNode) {
//                if (TOPIC_DD.equals(previous)) {
//                    html.endElement(); // dlentry
//                    html.startElement(TOPIC_DLENTRY, DLENTRY_ATTS);
//                }
//            }
//            context.renderChildren(child);
//            previous = (child instanceof DefinitionTermNode) ? TOPIC_DT : TOPIC_DD;
//        }
        html.endElement(); // dlentry
        html.endElement(); // dl
    }

    private void render(final DefinitionItem node, final NodeRendererContext context, final DitaWriter html) {
        printTag(node, context, html, TOPIC_DD, DD_ATTS);
    }

    private void render(final DefinitionTerm node, final NodeRendererContext context, final DitaWriter html) {
        if (html.tagStack.peek().equals(TOPIC_DL)) {
            html.startElement(TOPIC_DLENTRY, DLENTRY_ATTS);
        }
        printTag(node, context, html, TOPIC_DT, DT_ATTS);
    }

//    private void render(final ExpImageNode node, final NodeRendererContext context, final DitaWriter html) {
//        writeImage(node, node.title, null, node.url, null);
//    }

    private void writeImage(ImageRef node, final String title, final String alt, final String url, final String key, final NodeRendererContext context, DitaWriter html) {
        final AttributesBuilder atts = new AttributesBuilder(IMAGE_ATTS)
                .add(ATTRIBUTE_NAME_HREF, url);
        if (key != null) {
            atts.add(ATTRIBUTE_NAME_KEYREF, key);
        }

        if (!title.isEmpty()) {
            html.startElement(TOPIC_FIG, FIG_ATTS);
            html.startElement(TOPIC_TITLE, TITLE_ATTS);
            html.characters(title);
            html.endElement();
            html.startElement(TOPIC_IMAGE, atts.build());
            if (hasChildren(node)) {
                html.startElement(TOPIC_ALT, ALT_ATTS);
                if (alt != null) {
                    html.characters(alt);
                } else {
                    context.renderChildren(node);
                }
                html.endElement();
            }
            html.endElement();
            html.endElement();
        } else if (onlyImageChild) {
            atts.add("placement", "break");
            html.startElement(TOPIC_IMAGE, atts.build());
            if (hasChildren(node)) {
                html.startElement(TOPIC_ALT, ALT_ATTS);
                if (alt != null) {
                    html.characters(alt);
                } else {
                    context.renderChildren(node);
                }
                html.endElement();
            }
            html.endElement();
        } else {
            html.startElement(TOPIC_IMAGE, atts.build());
            if (hasChildren(node)) {
                html.startElement(TOPIC_ALT, ALT_ATTS);
                if (alt != null) {
                    html.characters(alt);
                } else {
                    context.renderChildren(node);
                }
                html.endElement();
            }
            html.endElement();
        }
    }

//    @Override
//    private void render(final ExpLinkNode node, final NodeRendererContext context, final DitaWriter html) {
//        final AttributesBuilder atts = getLinkAttributes(node.url);
//        //atts.add("title", node.title);
//
//        html.startElement(TOPIC_XREF, atts.build());
//        context.renderChildren(node);
//        html.endElement();
//    }

    private static <E> E containsSome(final Collection<E> col, final Collection<E> find) {
        for (final E c : col) {
            if (find.contains(c)) {
                return c;
            }
        }
        return null;
    }

    private void render(final Heading node, final NodeRendererContext context, final DitaWriter html) {
        if (node.getLevel() > headerLevel + 1) {
            throw new ParseException("Header level raised from " + headerLevel + " to " + node.getLevel() + " without intermediate header level");
        }
        final Title header = new Title(node);

        if (inSection) {
            html.endElement(); // section or example
            inSection = false;
        }
        final String section = containsSome(header.classes, sections.keySet());
        if (section != null) {
            if (node.getLevel() <= headerLevel) {
                throw new ParseException("Level " + node.getLevel() + " section title must be higher level than parent topic title " + headerLevel);
            }
            final DitaClass cls = sections.get(section);
            final AttributesBuilder atts = new AttributesBuilder()
                    .add(ATTRIBUTE_NAME_CLASS, cls.toString());
            if (node.getAnchorRefId() != null) {
                atts.add(ATTRIBUTE_NAME_ID, node.getAnchorRefId());
            } else {
                atts.add(ATTRIBUTE_NAME_ID, getId(header.title));
            }
            final Collection<String> classes = new ArrayList<>(header.classes);
            classes.removeAll(sections.keySet());
            if (!classes.isEmpty()) {
                atts.add("outputclass", String.join(" ", classes));
            }
            html.startElement(cls, atts.build());
            inSection = true;
            html.startElement(TOPIC_TITLE, TITLE_ATTS);
//            html.characters(header.title);
            context.renderChildren(node);
            html.endElement(); // title
        } else {
            if (headerLevel > 0) {
                html.endElement(); // body
            }
            for (; node.getLevel() <= headerLevel; headerLevel--) {
                html.endElement(); // topic
            }
            headerLevel = node.getLevel();

            final AttributesBuilder atts = new AttributesBuilder(TOPIC_ATTS);
            if (node.getAnchorRefId() != null) {
                atts.add(ATTRIBUTE_NAME_ID, node.getAnchorRefId());
            } else {
                atts.add(ATTRIBUTE_NAME_ID, getId(header.title));
            }
            if (!header.classes.isEmpty()) {
                atts.add("outputclass", String.join(" ", header.classes));
            }
            html.startElement(TOPIC_TOPIC, atts.build());
            html.startElement(TOPIC_TITLE, TITLE_ATTS);
//            html.characters(header.title);
            context.renderChildren(node);
            html.endElement(); // title
//            if (documentMetadata != null && !documentMetadata.isEmpty()) {
//                outputMetadata(documentMetadata, html);
//            }
            html.startElement(TOPIC_BODY, BODY_ATTS);
        }
    }

    private void outputMetadata(Map<String, Object> documentMetadata, DitaWriter html) {
        html.startElement(TOPIC_PROLOG, PROLOG_ATTS);
//        metadataSerializer.write(documentMetadata);
        html.endElement();
    }

    static class Metadata {
        final String id;
        final List<String> classes;

        Metadata(final String id, final List<String> classes) {
            this.id = id;
            this.classes = classes;
        }

        static Metadata parse(final String contents) {
            final String c = contents.trim();
            final List<String> classes = new ArrayList<>();
            String fragment = null;
            for (final String t : c.split("\\s+")) {
                if (t.startsWith("#")) {
                    fragment = t.substring(1);
                } else if (t.startsWith(".")) {
                    classes.add(t.substring(1));
                }
            }
            final String id = fragment != null ? fragment : null;
            return new Metadata(id, classes);
        }
    }

    static class Title {
        final String title;
        final String id;
        final Collection<String> classes;

        Title(final Heading node) {
            final String contents = node.toString();
            classes = new ArrayList<>();
            final Pattern p = Pattern.compile("^(.+?)(?:\\s*#{" + node.getLevel() + "}?\\s*)?(?:\\{(.+?)\\})?$");
            final Matcher m = p.matcher(contents);
            if (m.matches()) {
                title = m.group(1);
                if (m.group(2) != null) {
                    final Metadata metadata = Metadata.parse(m.group(2));
                    classes.addAll(metadata.classes);
                    id = metadata.id; // != null ? metadata.id : getId(title);
                } else {
//                    id = getId(title);
                    id = null;
                }
            } else {
                title = contents;
//                id = getId(contents);
                id = null;
            }
        }
    }

//    private String getId(final Heading node) {
//        return getId(toString(node));
//    }

    private static String getId(final String contents) {
        return contents.toLowerCase().replaceAll("[^\\w]", " ").trim().replaceAll("\\s+", "_");
    }

    private void render(final HtmlBlock node, final NodeRendererContext context, final DitaWriter html) {
        final String text = node.getChars().toString();
        html.characters(text);
    }

    private void render(final HtmlInline node, final NodeRendererContext context, final DitaWriter html) {
        html.characters(node.getChars().toString());
    }

    private void render(final ListItem node, final NodeRendererContext context, final DitaWriter html) {
        printTag(node, context, html, TOPIC_LI, LI_ATTS);
    }

    private void render(final MailLink node, final NodeRendererContext context, final DitaWriter html) {
        final AttributesBuilder atts = getLinkAttributes("mailto:" + node.getText());

        html.startElement(TOPIC_XREF, atts.build());
        context.renderChildren(node);
        html.endElement();
    }

    private void render(final OrderedList node, final NodeRendererContext context, final DitaWriter html) {
        printTag(node, context, html, TOPIC_OL, OL_ATTS);
    }

    private boolean onlyImageChild = false;

    private void render(final Paragraph node, final NodeRendererContext context, final DitaWriter html) {
//        if (containsImage(node)) {
//            onlyImageChild = true;
//            context.renderChildren(node);
//            onlyImageChild = false;
//        } else {
            printTag(node, context, html, TOPIC_P, P_ATTS);
//        }
    }

    /**
     * Contains only single image
     */
//    private boolean containsImage(final ContentNode node) {
//        if (node.getChildren().size() != 1) {
//            return false;
//        } else {
//            final Node first = node.getChildren().get(0);
//            if (first instanceof ExpImageNode || first instanceof RefImageNode) {
//                return true;
//            } else if (first instanceof SuperNode) {
//                return containsImage((SuperNode) first);
//            }
//        }
//        return false;
//    }
    private void render(final TypographicQuotes node, final NodeRendererContext context, final DitaWriter html) {
//        switch (node.getType()) {
//            case DoubleAngle:
//                html.characters('\u00AB');//&laquo;
//                context.renderChildren(node);
//                html.characters('\u00AB');//&laquo;
//                break;
//            case Double:
//                html.characters('\u201C');//"&ldquo;"
//                context.renderChildren(node);
//                html.characters('\u201C');//"&ldquo;"
//                break;
//            case Single:
        html.characters('\u2018');//"&lsquo;"
        context.renderChildren(node);
        html.characters('\u2018');//"&lsquo;"
//                break;
//        }
    }

    private void render(final ReferenceNode node, final NodeRendererContext context, final DitaWriter html) {
        // reference nodes are not printed
    }

    private void render(final Reference node, final NodeRendererContext context, final DitaWriter html) {
        // reference nodes are not printed
    }

    private void render(final ImageRef node, final NodeRendererContext context, final DitaWriter html) {
        final String text = node.toString();
        final String key = node.getReference() != null ? node.getReference().toString() : text;
        final ReferenceNode refNode = references.get(normalize(key));
        if (refNode == null) { // "fake" reference image link
            final Attributes atts = new AttributesBuilder(IMAGE_ATTS)
                    .add(ATTRIBUTE_NAME_KEYREF, key)
                    .build();
            html.startElement(TOPIC_IMAGE, atts);
            if (node.getReference() != null) {
                context.renderChildren(node);
            }
            html.endElement();
        } else {
            writeImage(node, refNode.toString(), text, node.getUrl().toString(), key, context, html);
        }
    }

    private void render(final RefNode node, final NodeRendererContext context, final DitaWriter html) {
        final String text = node.toString();
        final String key = node.getReference() != null ? node.getReference().toString() : text;
        final ReferenceNode refNode = references.get(normalize(key));
        if (refNode == null) { // "fake" reference link
            final AttributesBuilder atts = new AttributesBuilder(XREF_ATTS)
                    .add(ATTRIBUTE_NAME_KEYREF, key);
            html.startElement(TOPIC_XREF, atts.build());
            if (node.getReference() != null) {
                context.renderChildren(node);
            }
            html.endElement();
        } else {
            final AttributesBuilder atts = getLinkAttributes(node.getUrl().toString());
            html.startElement(TOPIC_XREF, atts.build());
            if (refNode.toString() != null) {
                html.characters(refNode.toString());
            } else {
                context.renderChildren(node);
            }
            html.endElement();
        }
    }

//    @Override
//    private void render(final SimpleNode node, final NodeRendererContext context, final DitaWriter html) {
//        switch (node.getType()) {
//            case Apostrophe:
//                html.characters('\u2019');//"&rsquo;"
//                break;
//            case Ellipsis:
//                html.characters('\u2026');//"&hellip;"
//                break;
//            case Emdash:
//                html.characters('\u2014');//"&mdash;"
//                break;
//            case Endash:
//                html.characters('\u2013');//"&ndash;"
//                break;
//            case HRule:
////                html.startElement(TOPIC_PH, new AttributesBuilder().add(ATTRIBUTE_NAME_CLASS, "+ html-d/hr ").build());
////                html.endElement();
//                html.characters('\n');
//                break;
//            case Linebreak:
////                html.startElement(TOPIC_PH, new AttributesBuilder().add(ATTRIBUTE_NAME_CLASS, "+ html-d/br ").build());
////                html.endElement();
//                html.characters('\n');
//                break;
//            case Nbsp:
//                html.characters("\u00A0");//&nbsp;
//                break;
//            default:
//                throw new ParseException("Unsupported simple node: " + node.getType());
//        }
//    }

//    private static String toString(final SimpleNode node) {
//        switch (node.getType()) {
//            case Apostrophe:
//                return "\u2019";//"&rsquo;"
//            case Ellipsis:
//                return "\u2026";//"&hellip;"
//            case Emdash:
//                return "\u2014'";//"&mdash;"
//            case Endash:
//                return "\u2013";//"&ndash;"
//            case Linebreak:
//            case HRule:
//                return "\n";
//            case Nbsp:
//                return "\u00A0";//&nbsp;
//            default:
//                throw new ParseException("Unsupported simple node: " + node.getType());
//        }
//    }


//    @Override
//    private void render(final StrongEmphSuperNode node, final NodeRendererContext context, final DitaWriter html) {
//        if (node.isClosed()) {
//            if (node.isStrong()) {
//                printTag(node, HI_D_B, B_ATTS);
//            } else {
//                printTag(node, HI_D_I, I_ATTS);
//            }
//        } else {
//            //sequence was not closed, treat open chars as ordinary chars
//            html.characters(node.getChars());
//            context.renderChildren(node);
//        }
//    }

    private void render(final Strikethrough node, final NodeRendererContext context, final DitaWriter html) {
        printTag(node, context, html, TOPIC_PH, DEL_ATTS);
    }

    private void render(final Emphasis node, final NodeRendererContext context, final DitaWriter html) {
        printTag(node, context, html, HI_D_I, I_ATTS);
    }

    private void render(final StrongEmphasis node, final NodeRendererContext context, final DitaWriter html) {
        printTag(node, context, html, HI_D_B, B_ATTS);
    }

    private void render(final TableBody node, final NodeRendererContext context, final DitaWriter html) {
        printTag(node, context, html, TOPIC_TBODY, TBODY_ATTS);
    }

//    @Override
//    private void render(final TableCaptionNode node, final NodeRendererContext context, final DitaWriter html) {
//        html.startElement(TOPIC_TITLE, TITLE_ATTS);
//        context.renderChildren(node);
//        html.endElement();
//    }

//    @Override
//    private void render(final TableCell node, final NodeRendererContext context, final DitaWriter html) {
//        final List<TableColumnNode> columns = currentTableNode.getColumns();
//        final TableColumnNode column = columns.get(Math.min(currentTableColumn, columns.size() - 1));
//
//        final AttributesBuilder atts = new AttributesBuilder(ENTRY_ATTS);
//        column.accept(this);
//        if (tableColumnAlignment != null) {
//            atts.add(ATTRIBUTE_NAME_ALIGN, tableColumnAlignment);
//            tableColumnAlignment = null;
//        }
//        if (node.getColSpan() > 1) {
//            atts.add(ATTRIBUTE_NAME_NAMEST, COLUMN_NAME_COL + Integer.toString(currentTableColumn + 1));
//            atts.add(ATTRIBUTE_NAME_NAMEEND, COLUMN_NAME_COL + Integer.toString(currentTableColumn + node.getColSpan()));
//        }
//        html.startElement(TOPIC_ENTRY, atts.build());
//        context.renderChildren(node);
//        html.endElement();
//
//        currentTableColumn += node.getColSpan();
//    }

    private String tableColumnAlignment = null;

//    @Override
//    private void render(final TableColumnNode node, final NodeRendererContext context, final DitaWriter html) {
//        switch (node.getAlignment()) {
//            case None:
//                break;
//            case Left:
//                tableColumnAlignment = "left";
//                break;
//            case Right:
//                tableColumnAlignment = "right";
//                break;
//            case Center:
//                tableColumnAlignment = "center";
//                break;
//            default:
//                throw new ParseException("Unsupported table column alignment: " + node.getAlignment());
//        }
//    }

    private void render(final TableHead node, final NodeRendererContext context, final DitaWriter html) {
        printTag(node, context, html, TOPIC_THEAD, THEAD_ATTS);
    }

    private void render(final TableBlock node, final NodeRendererContext context, final DitaWriter html) {
        currentTableNode = node;
        html.startElement(TOPIC_TABLE, TABLE_ATTS);
//        for (final Node child : node.getChildren()) {
//            if (child instanceof TableCaptionNode) {
//                context.renderChildren(child);
//            }
//        }
        final Attributes atts = new AttributesBuilder(TGROUP_ATTS)
                .add(ATTRIBUTE_NAME_COLS, Integer.toString(node.getSegments().length))
                .build();
        html.startElement(TOPIC_TGROUP, atts);

//        int counter = 1;
//        for (final TableColumnNode col : node.getColumns()) {
//            final AttributesBuilder catts = new AttributesBuilder(COLSPEC_ATTS)
//                    .add(ATTRIBUTE_NAME_COLNAME, COLUMN_NAME_COL + counter);
//            switch (col.getAlignment()) {
//                case Center:
//                    catts.add(ATTRIBUTE_NAME_ALIGN, "center");
//                    break;
//                case Right:
//                    catts.add(ATTRIBUTE_NAME_ALIGN, "right");
//                    break;
//                case Left:
//                    catts.add(ATTRIBUTE_NAME_ALIGN, "left");
//                    break;
//            }
//            html.startElement(TOPIC_COLSPEC, catts.build());
//            html.endElement(); // colspec
//            counter++;
//        }
//        for (final Node child : node.getChildren()) {
//            if (!(child instanceof TableCaptionNode)) {
//                context.renderChildren(child);
//            }
//        }
        html.endElement(); // tgroup
        html.endElement(); // table
        currentTableNode = null;
    }

    private void render(final TableRow node, final NodeRendererContext context, final DitaWriter html) {
        currentTableColumn = 0;
        printTag(node, context, html, TOPIC_ROW, TR_ATTS);
    }

    private void render(final CodeBlock node, final NodeRendererContext context, final DitaWriter html) {
        final AttributesBuilder atts = new AttributesBuilder(CODEBLOCK_ATTS)
                .add(XML_NS_URI, "space", "xml:space", "CDATA", "preserve");
//        if (node.getType() != null && !node.getType().isEmpty()) {
//            final String type = node.getType().trim();
//            final Metadata metadata;
//            if (type.startsWith("{")) {
//                metadata = Metadata.parse(type.substring(1, type.length() - 1));
//            } else {
//                metadata = new Metadata(null, Collections.singletonList(type));
//            }
//            if (metadata.id != null) {
//                atts.add(ATTRIBUTE_NAME_ID, metadata.id);
//            }
//            if (!metadata.classes.isEmpty()) {
//                atts.add("outputclass", String.join(" ", metadata.classes));
//            }
//        }
        html.startElement(PR_D_CODEBLOCK, atts.build());
        String text = node.getChars().toString();
        if (text.endsWith("\n")) {
            text = text.substring(0, text.length() - 1);
        }
        html.characters(text);
        html.endElement();
    }

//    @Override
//    private void render(final WikiLinkNode node, final NodeRendererContext context, final DitaWriter html) {
//        String url;
//        try {
//            url = "./" + URLEncoder.encode(node.getChars().toString().replace(' ', '-'), "UTF-8") + ".html";
//        } catch (final UnsupportedEncodingException e) {
//            throw new IllegalArgumentException(e);
//        }
//        final AttributesBuilder atts = getLinkAttributes(url);
//
//        html.startElement(TOPIC_XREF, atts.build());
//        //html.characters(rendering.text);
//        context.renderChildren(node);
//        html.endElement();
//    }

    private void render(final Text node, final NodeRendererContext context, final DitaWriter html) {
        if (abbreviations.isEmpty()) {
            html.characters(node.getChars().toString());
        } else {
            printWithAbbreviations(node.getChars().toString(), html);
        }
    }

//    @Override
//    private void render(final SpecialText node, final NodeRendererContext context, final DitaWriter html) {
//        html.characters(node.getChars().toString());
//    }

    private void render(final ContentNode node, final NodeRendererContext context, final DitaWriter html) {
        context.renderChildren(node);
    }

    private void render(final SoftLineBreak node, final NodeRendererContext context, final DitaWriter html) {
        html.characters('\n');
    }

    private void render(final Node node, final NodeRendererContext context, final DitaWriter html) {
        throw new RuntimeException("No renderer configured for " + node.getNodeName());
//        if (node instanceof MetadataNode) {
//            final MetadataNode n = (MetadataNode) node;
//            final String id = getId(n.title);
//            html.startElement(TOPIC_TOPIC, new AttributesBuilder(TOPIC_ATTS).add(ATTRIBUTE_NAME_ID, id).build());
//            html.startElement(TOPIC_TITLE, TITLE_ATTS);
//            html.characters(n.title);
//            html.endElement();
//        } else {
//            context.renderChildren(node);
//        }
//        for (final ToDitaSerializerPlugin plugin : plugins) {
//            if (plugin.visit(node, this, printer)) {
//                return;
//            }
//        }
    }

    // helpers

    private boolean hasChildren(final Node node) {
        return node.hasChildren();
//        if (node instanceof SuperNode) {
//            return !node.hasChildren();
//        } else if (node instanceof Text) {
//            return !((Text) node).hasChildren();
//        } else {
//            throw new UnsupportedOperationException();
//        }
    }

//    private void visitChildren(final Node node) {
//        for (final Node child : node.getChildren()) {
//            child.accept(this);
//        }
//    }

    private void printTag(Text node, NodeRendererContext context, DitaWriter html, final DitaClass tag, final Attributes atts) {
        html.startElement(tag, atts);
        html.characters(node.getChars().toString());
        html.endElement();
    }

    private void printTag(Node node, NodeRendererContext context, DitaWriter html, final DitaClass tag, final Attributes atts) {
        html.startElement(tag, atts);
        context.renderChildren(node);
        html.endElement();
    }

    private AttributesBuilder getLinkAttributes(final String href) {
        final AttributesBuilder atts = new AttributesBuilder(XREF_ATTS)
                .add(ATTRIBUTE_NAME_HREF, href);

        final String ext = FilenameUtils.getExtension(href).toLowerCase();
        String format;
        switch (ext) {
            case ATTR_FORMAT_VALUE_DITA:
            case "xml":
                format = null;
                break;
            // Markdown is converted to DITA
            case "md":
            case "markdown":
                format = "markdown";
                break;
            default:
                format = !ext.isEmpty() ? ext : "html";
                break;
        }
        if (format != null) {
            atts.add(ATTRIBUTE_NAME_FORMAT, format);
        }

        final URI uri = toURI(href);
        if (uri != null && uri.isAbsolute()) {
            atts.add(ATTRIBUTE_NAME_SCOPE, ATTR_SCOPE_VALUE_EXTERNAL);
        }

        return atts;
    }

    private String normalize(final String string) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < string.length(); i++) {
            char c = string.charAt(i);
            switch (c) {
                case ' ':
                case '\n':
                case '\t':
                    continue;
            }
            sb.append(Character.toLowerCase(c));
        }
        return sb.toString();
    }

    private void printWithAbbreviations(String string, final DitaWriter html) {
        Map<Integer, Map.Entry<String, String>> expansions = null;

        for (Map.Entry<String, String> entry : abbreviations.entrySet()) {
            // first check, whether we have a legal match
            String abbr = entry.getKey();

            int ix = 0;
            while (true) {
                final int sx = string.indexOf(abbr, ix);
                if (sx == -1) break;

                // only allow whole word matches
                ix = sx + abbr.length();

                if (sx > 0 && Character.isLetterOrDigit(string.charAt(sx - 1))) continue;
                if (ix < string.length() && Character.isLetterOrDigit(string.charAt(ix))) {
                    continue;
                }

                // ok, legal match so save an expansions "task" for all matches
                if (expansions == null) {
                    expansions = new TreeMap<>();
                }
                expansions.put(sx, entry);
            }
        }

        if (expansions != null) {
            int ix = 0;
            for (Map.Entry<Integer, Map.Entry<String, String>> entry : expansions.entrySet()) {
                int sx = entry.getKey();
                final String abbr = entry.getValue().getKey();
                final String expansion = entry.getValue().getValue();

                html.characters(string.substring(ix, sx));
                final AttributesBuilder atts = new AttributesBuilder(PH_ATTS);
                if (expansion != null && !expansion.isEmpty()) {
                    atts.add(ATTRIBUTE_NAME_OTHERPROPS, expansion);
                }
                html.startElement(TOPIC_PH, atts.build());
                html.characters(abbr);
                html.endElement();
                ix = sx + abbr.length();
            }
            html.characters(string.substring(ix));
        } else {
            html.characters(string);
        }
    }

//    private class MetadataNode extends Node {
//        final String title;
//
//        MetadataNode(final String title) {
//            this.title = title;
//        }
//
//        @Override
//        public void accept(final Visitor visitor) {
//            visitor.visit(this);
//        }
//
//        @Override
//        public List<Node> getChildren() {
//            return Collections.emptyList();
//        }
//
//        @Override
//        public BasedSequence[] getSegments() {
//            return new BasedSequence[0];
//        }
//    }

    public static class Factory implements NodeRendererFactory {
        @Override
        public NodeRenderer create(final DataHolder options) {
            return new CoreNodeRenderer(options);
        }
    }

}

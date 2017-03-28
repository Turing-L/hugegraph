package com.baidu.hugegraph2.structure;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Property;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.structure.VertexProperty;

import com.baidu.hugegraph2.HugeException;
import com.baidu.hugegraph2.HugeGraph;
import com.baidu.hugegraph2.backend.id.Id;
import com.baidu.hugegraph2.backend.id.SplicingIdGenerator;
import com.baidu.hugegraph2.type.HugeTypes;
import com.baidu.hugegraph2.type.schema.EdgeLabel;

public class HugeEdge extends HugeElement implements Edge, Cloneable {

    protected EdgeLabel label;

    protected HugeVertex owner; // the Vertex who owned me
    protected HugeVertex sourceVertex;
    protected HugeVertex targetVertex;

    public HugeEdge(final HugeGraph graph, Id id, EdgeLabel label) {
        super(graph, id);
        this.label = label;
    }

    @Override
    public HugeTypes type() {
        // NOTE: we optimize the edge type that let it include direction
        return this.owner == this.sourceVertex ? HugeTypes.EDGE_OUT : HugeTypes.EDGE_IN;
    }

    @Override
    public String name() {
        List<String> properties = new LinkedList<>();
        for (String key : this.edgeLabel().sortKeys()) {
            properties.add(this.property(key).value().toString());
        }
        // TODO: use a better delimiter
        return String.join(SplicingIdGenerator.NAME_SPLITOR, properties);
    }

    @Override
    public String label() {
        return this.label.name();
    }

    public EdgeLabel edgeLabel() {
        return this.label;
    }

    public Direction direction() {
        if (!this.label.isDirected()) {
            return Direction.BOTH;
        }

        if (this.type() == HugeTypes.EDGE_OUT) {
            return Direction.OUT;
        }
        else {
            return Direction.IN;
        }
    }

    public void assignId() {
        assert this.id == null;
        // generate an id and assign
        if (this.id == null) {
            this.id = SplicingIdGenerator.generate(this);
        }
    }

    @Override
    public <V> Property<V> property(String key, V value) {
        HugeProperty<V> prop = new HugeProperty<V>(this, key, value);
        return super.setProperty(prop) != null ? prop : null;
    }

    @Override
    public void remove() {
        // TODO Auto-generated method stub
    }

    @Override
    public <V> Iterator<Property<V>> properties(String... propertyKeys) {
        List<Property<V>> propertyList = new ArrayList<>(propertyKeys.length);
        for (String pk : propertyKeys) {
            HugeProperty<? extends Object> prop = this.getProperty(pk);
            assert prop instanceof VertexProperty;
            propertyList.add((Property<V>) prop);
        }
        return propertyList.iterator();
    }

    @Override
    public Iterator<Vertex> vertices(Direction direction) {
        List<Vertex> vertices = new ArrayList<>(2);
        switch (direction) {
            case OUT:
                vertices.add(this.sourceVertex);
                break;
            case IN:
                vertices.add(this.targetVertex);
                break;
            case BOTH:
                vertices.add(this.sourceVertex);
                vertices.add(this.targetVertex);
                break;
            default:
                break;
        }
        return vertices.iterator();
    }

    public void vertices(HugeVertex source, HugeVertex target) {
        this.owner = source; // The default owner is the source vertex
        this.sourceVertex = source;
        this.targetVertex = target;
    }

    public HugeEdge switchOwner() {
        HugeEdge edge = null;
        try {
            edge = (HugeEdge) this.clone();
        } catch (CloneNotSupportedException e) {
            throw new HugeException("Failed to clone HugeEdge", e);
        }

        if (edge.owner == edge.sourceVertex) {
            edge.owner = edge.targetVertex;
        }
        else {
            edge.owner = edge.sourceVertex;
        }

        return edge;
    }

    public HugeVertex owner() {
        return this.owner;
    }

    public void owner(HugeVertex owner) {
        this.owner = owner;
    }

    public HugeVertex sourceVertex() {
        return this.sourceVertex;
    }

    public void sourceVertex(HugeVertex sourceVertex) {
        this.sourceVertex = sourceVertex;
    }

    public HugeVertex targetVertex() {
        return this.targetVertex;
    }

    public void targetVertex(HugeVertex targetVertex) {
        this.targetVertex = targetVertex;
    }

    public boolean belongToLabels(String... edgeLabels) {
        if (edgeLabels.length == 0) {
            return true;
        }

        // does edgeLabels contain me
        for (String label : edgeLabels) {
            if (label.equals(this.label())) {
                return true;
            }
        }
        return false;
    }

    public HugeVertex otherVertex(HugeVertex vertex) {
        if (vertex == this.sourceVertex) {
            return this.targetVertex;
        }
        else {
            return this.sourceVertex;
        }
    }

    public HugeVertex otherVertex() {
        return this.otherVertex(this.owner);
    }

    @Override
    public String toString() {
        return String.format("{id=%s, label=%s,"
                + " source=%s, target=%s, direction=%s, properties=%s}",
                this.id,
                this.label.name(),
                this.sourceVertex.id(),
                this.targetVertex.id(),
                this.direction().name(),
                this.properties.values());
    }
}
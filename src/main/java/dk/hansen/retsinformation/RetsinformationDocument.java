package dk.hansen.retsinformation;

import java.util.Objects;

record RetsinformationDocument(
        String id,
        String titel,
        String type,
        String dato,
        String tekst
) {
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RetsinformationDocument that = (RetsinformationDocument) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(titel, that.titel) &&
                Objects.equals(type, that.type) &&
                Objects.equals(dato, that.dato) &&
                Objects.equals(tekst, that.tekst);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, titel, type, dato, tekst);
    }
}
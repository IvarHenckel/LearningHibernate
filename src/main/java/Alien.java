import javax.persistence.*;

@Entity// @Entity(name = "alien_table") can be used to change the entity name. (This can indirectly change the table name since table name is by default entity name.)
@Table(name = "alien_table")
public class Alien { // with Entity we know that we are working a database entity
    @Id
    private int aid; // Id marks that this is our primary key.
    //@Transient // With this annotation, aname will not be stored in the table
    private AlienName aname;
    @Column(name="alien_color") // This specifies the name of the column, by default the variable name is used
    private String color;

    public int getAid() {
        return aid;
    }

    public void setAid(int aid) {
        this.aid = aid;
    }

    public AlienName getAname() {
        return aname;
    }

    public void setAname(AlienName aname) {
        this.aname = aname;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    @Override
    public String toString() {
        return "Alien [aid=" + aid + " aname=[" + aname.toString() + "] color=" + color + "]";
    }
}

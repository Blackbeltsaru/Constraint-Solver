/*==============================
 * Name:   Ryan Biggs
 * Date:   10/15/14
 * Course: CSE421
 *==============================*/

public class DomainWipeoutException extends RuntimeException
{
	private static final long serialVersionUID = 1L;

	public DomainWipeoutException()
	{
        super();
    }

    public DomainWipeoutException(String message)
    {
        super(message);
    }
}

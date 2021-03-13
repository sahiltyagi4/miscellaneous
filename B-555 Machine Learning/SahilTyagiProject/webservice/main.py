def hello_world(request):
    """Responds to any HTTP request.
    Args:
        request (flask.Request): HTTP request object.
    Returns:
        The response text or any set of values that can be turned into a
        Response object using
        `make_response <http://flask.pocoo.org/docs/1.0/api/#flask.Flask.make_response>`.
    """

    book = {}
    book['Emma'] = 'emma,churchill,hartfield'
    book['Lady Susan'] = 'courcy,mother,james'
    book['Love and Freindship'] = 'father,married,affection'
    book['Mansfield Park'] = 'house,thomas,norris'
    book['Northanger Abbey'] = 'henry,eleanor,friend'
    book['Persuasion'] = 'captain,musgrove,elizabeth'
    book['Pride and Prejudice'] = 'family,bennet,darcy'
    book['Sense and Sensibility'] = 'colonel,heart,dashwood'
    book['The Letters of Jane Austen'] = 'london,love,woman'
    book['The Watsons/ By Jane Austen and Concluded by L. Oulton'] = 'country,marry,honor'

    if request.method == 'GET':
        book_name = request.args.get('link')
        topics = book[book_name]
            
        return str(topics)
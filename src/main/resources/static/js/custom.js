/**
 * Sends an "ajax" request using Fetch. Sends JSON and expects JSON back.
 *
 * @param {string} url
 * @param {string} method (GET|POST)
 * @param {*} data, typically a JSON-izable object, like a Message
 * @param {*} headers, to be used instead of defaults, if specified. To send NO headers,
 *  use {}. To send defaults, specify no value, or use false
 *
 * @return {Promise}, which you should chain with `.then()` to manage responses,
 *             and with `.catch()` to manage possible errors.
 *             Errors will be notified as
 *  {
 *     url: <that you were accessing>,
 *     data: <data you sent>,
 *     status: <code, such as 403>,
 *     text: <describing the error>
 *  }
 */
function go(url, method, data = {}, headers = false) {
    let params = {
        method: method, // POST, GET, POST, PUT, DELETE, etc.
        headers: headers === false ? {
            "Content-Type": "application/json; charset=utf-8",
        } : headers,
        body: data instanceof FormData ? data : JSON.stringify(data)
    };
    if (method === "GET") {
        // GET requests cannot have body; I could URL-encode, but it would not be used here
        delete params.body;
    } else {
        params.headers["X-CSRF-TOKEN"] = config.csrf.value;
    }
    console.log("sending", url, params)
    return fetch(url, params)
        .then(response => {
            const r = response;
            if (r.ok) {
                return r.json().then(json => Promise.resolve(json));
            } else {
                return r.text().then(text => Promise.reject({
                    url,
                    data: JSON.stringify(data),
                    status: r.status,
                    text
                }));
            }
        });
}

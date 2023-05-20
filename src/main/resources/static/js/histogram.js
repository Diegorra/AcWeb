/*function convertData(data, source) {
    return data.map(dp => ({ sub1: source, sub2: dp.b, value: dp.d }));
}*/

function getDataInRange(min, max, data) {
    return data.filter(d => (d.value >= min && d.value < max));
}

// Copyright 2021 Observable, Inc.
// Released under the ISC license.
// https://observablehq.com/@d3/histogram
function Histogram(data, {
    value = d => d, // convenience alias for x
    domain, // convenience alias for xDomain
    label, // convenience alias for xLabel
    format, // convenience alias for xFormat
    type = d3.scaleLinear, // convenience alias for xType
    x = value, // given d in data, returns the (quantitative) x-value
    y = () => 1, // given d in data, returns the (quantitative) weight
    thresholds = 40, // approximate number of bins to generate, or threshold function
    normalize, // whether to normalize values to a total of 100%
    marginTop = 20, // top margin, in pixels
    marginRight = 30, // right margin, in pixels
    marginBottom = 30, // bottom margin, in pixels
    marginLeft = 40, // left margin, in pixels
    width = 640, // outer width of chart, in pixels
    height = 400, // outer height of chart, in pixels
    insetLeft = 0.5, // inset left edge of bar
    insetRight = 0.5, // inset right edge of bar
    xType = type, // type of x-scale
    xDomain = [0, 1], // [xmin, xmax]
    xRange = [marginLeft, width - marginRight], // [left, right]
    xLabel = label, // a label for the x-axis
    xFormat = format, // a format specifier string for the x-axis
    yType = d3.scaleLinear, // type of y-scale
    yDomain, // [ymin, ymax]
    yRange = [height - marginBottom, marginTop], // [bottom, top]
    yLabel = "↑ Frequency", // a label for the y-axis
    yFormat = "d", // a format specifier string for the y-axis
    color = "currentColor", // bar fill color
    responsive,
    listElement
} = {}) {
    // Compute values.
    const X = d3.map(data, x);
    const Y0 = d3.map(data, y);
    const I = d3.range(X.length);

    // Compute bins.
    const bins = d3.bin().thresholds(thresholds).value(i => X[i])(I);
    const Y = Array.from(bins, I => d3.sum(I, i => Y0[i]));
    if (normalize) {
        const total = d3.sum(Y);
        for (let i = 0; i < Y.length; ++i) Y[i] /= total;
    }

    // Compute default domains.
    if (xDomain === undefined) xDomain = [bins[0].x0, bins[bins.length - 1].x1];
    if (yDomain === undefined) yDomain = [0, d3.max(Y)];

    // Compute numTicks in y axis based on Sturges rule
    var numTicks = Math.ceil(1 + Math.log2(d3.max(Y)));  // Regla de Sturges

    // Construct scales and axes.
    const xScale = xType(xDomain, xRange);
    const yScale = yType(yDomain, yRange);
    const xAxis = d3.axisBottom(xScale).ticks(width / 80, xFormat).tickSizeOuter(0);
    const yAxis = d3.axisLeft(yScale).ticks(numTicks, yFormat);
    yFormat = yScale.tickFormat(numTicks, yFormat);

    const onClick = function (event, d) {
        if (responsive) {
            var dataInRange = getDataInRange(d.x0, d.x1, data);
            console.log(dataInRange);
            var list = d3.select(`#${listElement}`);
            console.log(document.getElementById(listElement).getAttribute("analysisId"));
            list.html("");
            list.append("h2")
                .text(`Range: ${d.x0} - ${d.x1}`)
            list.selectAll("a")
                .data(dataInRange)
                .enter()
                .append("a")
                .attr("href", d => `/analysis/${document.getElementById(listElement).getAttribute("analysisId")}/get/${d.sub1}/${d.sub2}`)
                .attr("title", d => `${d.sub1} y ${d.sub2} tienen distancia: ${d.value}`)
                .text(d => `${d.sub1} - ${d.sub2}`)
                .append("br");
        }
    }

    const svg = d3.create("svg")
        .attr("width", width)
        .attr("height", height)
        .attr("viewBox", [0, 0, width, height])
        .attr("style", "max-width: 100%; height: auto; height: intrinsic;");

    // Adding y axis to sgv
    svg.append("g")
        .attr("transform", `translate(${marginLeft},0)`)
        .call(yAxis)
        .call(g => g.select(".domain").remove())
        .call(g => g.selectAll(".tick line").clone()
            .attr("x2", width - marginLeft - marginRight)
            .attr("stroke-opacity", 0.1))
        .call(g => g.append("text")
            .attr("x", -marginLeft)
            .attr("y", 10)
            .attr("fill", "currentColor")
            .attr("text-anchor", "start")
            .text(yLabel));

    // Adding bars to svg
    svg.append("g")
        .attr("fill", color)
        .selectAll("rect")
        .data(bins)
        .join("rect")
            .attr("x", d => xScale(d.x0) + insetLeft)
            .attr("width", d => Math.max(0, xScale(d.x1) - xScale(d.x0) - insetLeft - insetRight))
            .attr("y", (d, i) => yScale(Y[i]))
            .attr("height", (d, i) => yScale(0) - yScale(Y[i]))
            .on("click", onClick)
            //.attr("fill", d => `rgb(${((1 - d.x0) * 255)}, ${255 - ((1 - d.x0) * 255)}, 0)`)
        .append("title")
        .text((d, i) => [`${d.x0} ≤ x < ${d.x1}`, yFormat(Y[i])].join("\n"));

    // Adding x axis to svg
    svg.append("g")
        .attr("transform", `translate(0,${height - marginBottom})`)
        .call(xAxis)
        .call(g => g.append("text")
            .attr("x", width - marginRight)
            .attr("y", 27)
            .attr("fill", "currentColor")
            .attr("text-anchor", "end")
            .text(xLabel));

    return svg.node();
}

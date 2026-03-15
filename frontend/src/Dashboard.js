import React from "react";

export default function Dashboard({ user }) {
	return (
	 	<div>
	    	<h2>Welcome, {user.username ?? user.email ?? 'user'}!</h2>
	    	<hr />
	    	<h3>Account details</h3>
	    	<pre style={{ whiteSpace: 'pre-wrap' }}>{JSON.stringify(user, null, 2)}</pre>
		</div>
	);
}
